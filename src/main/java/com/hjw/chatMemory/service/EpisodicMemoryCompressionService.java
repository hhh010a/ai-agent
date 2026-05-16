package com.hjw.chatMemory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjw.chatMemory.pojo.MemoryExtract;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@Async("memoryExecutor")
public class EpisodicMemoryCompressionService {
    private final ChatClient memoryExtractionClient;

    private final VectorStore vectorStore;

    private static final double DEDUP_SIMILARITY_THRESHOLD = 0.85;

    private static final double MIN_IMPORTANCE = 0.5;

    public EpisodicMemoryCompressionService(
            ChatClient memoryExtractionClient,
            VectorStore vectorStore) {
        this.memoryExtractionClient = memoryExtractionClient;
        this.vectorStore = vectorStore;
    }

    public void compressAndStore(String conversationId, List<Message> messages){
        if(messages==null||messages.isEmpty()){
            return;
        }
        try {
            String prompt = buildExtractionPrompt(messages);

            List<MemoryExtract> extracts = callExtractionModel(prompt);
            if (extracts.isEmpty()) {
                return;
            }

            extracts = filteringAndDeduplication(extracts);

            storeMemories(conversationId, extracts);
        } catch (Exception e) {
            log.error("压缩记忆失败 conversationId={}", conversationId,  e);
        }
    }


    private String buildExtractionPrompt(List<Message> messages) {
        StringBuilder conversation = new StringBuilder();
        for (Message msg : messages) {
            String role = msg instanceof UserMessage ? "User" : "Assistant";
            conversation.append(role).append(": ").append(msg.getText()).append("\n");
        }
        return """
            根据以下对话，提取最多5条值得长期记忆的重要信息。
            
            提取规则：
            1. 优先提取用户的信息：姓名、身份、学习目标、遇到的问题、需求、偏好、计划、任务
            2. 从AI回答中只提取与用户直接相关的内容：学习建议、解决方案、约定事项、任务安排
            3. 忽略AI回答中的解释性内容、示例代码、通用知识
            
            每条信息用JSON格式：{"summary": "...", "importance": 0.0~1.0}
            summary用中文，不超过30字。importance表示记忆对未来交互的价值（用户信息权重更高）。
            只返回JSON数组，不添加任何解释。

            对话：
            %s
            """.formatted(conversation.toString());
    }

    private List<MemoryExtract> callExtractionModel(String prompt) {
        try {
            String response = memoryExtractionClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // 清洗可能的标记符号，确保是纯JSON数组
            String cleanJson = response.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7, cleanJson.length() - 3).trim();
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3, cleanJson.length() - 3).trim();
            }

            // 解析JSON
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(cleanJson, new TypeReference<List<MemoryExtract>>() {});

        } catch (Exception e) {
            log.warn("记忆提取解析失败，原始输出: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<MemoryExtract> filteringAndDeduplication(List<MemoryExtract> extracts){
        List<MemoryExtract> valid = new ArrayList<>();
        for (MemoryExtract ext : extracts) {
            //重要性过滤
            if (ext.getImportance() < MIN_IMPORTANCE) {
                continue;
            }

            //相似性去重：检查向量库中是否已有高度相似的内容
            if (isDuplicate(ext.getSummary())) {
                log.debug("记忆已存在，跳过: {}", ext.getSummary());
                continue;
            }

            valid.add(ext);
        }
        return valid;
    }

    private boolean isDuplicate(String summary) {
        try {
            // 用当前summary做一次快速相似搜索
            SearchRequest request = SearchRequest
                    .builder()
                    .query(summary)
                    .topK(1)
                    .similarityThreshold(DEDUP_SIMILARITY_THRESHOLD)
                    .build();
            List<Document> result = vectorStore.similaritySearch(request);
            return !result.isEmpty();
        } catch (Exception e) {
            // 向量库查询失败时，不进行去重，直接存入
            log.warn("去重查询失败，跳过此步骤", e);
            return false;
        }
    }

    private void storeMemories(String conversationId, List<MemoryExtract> extracts) {
        Instant now = Instant.now();
        List<Document> docs = new ArrayList<>();

        for (MemoryExtract ext : extracts) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("conversationId", conversationId);
            metadata.put("timestamp", now.toEpochMilli());
            metadata.put("importance", ext.getImportance());
            metadata.put("lastAccessedAt", now.toEpochMilli());
            metadata.put("type", "episodic");

            Document doc = new Document(ext.getSummary(), metadata);
            docs.add(doc);
        }

        // 批量写入向量数据库
        vectorStore.add(docs);
        log.info("成功存入{}条长期记忆 conversationId={}", docs.size(),conversationId);
    }
}
