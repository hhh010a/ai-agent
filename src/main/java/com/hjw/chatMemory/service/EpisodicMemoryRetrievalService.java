package com.hjw.chatMemory.service;

import lombok.AllArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EpisodicMemoryRetrievalService {

    private final VectorStore vectorStore;

    //衰减系数 越大遗忘越快
    private static final double DECAY_LAMBDA = 0.01;

    public EpisodicMemoryRetrievalService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> recallAndRank(String query, String conversationId, int topK) {
        // 1. 向量语义搜索 + 元数据筛选
        List<Document> rawResults = semanticSearch(query, conversationId);
        if (rawResults.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 衰减重排序
        return rerankByDecay(rawResults, topK);
    }



    private List<Document> semanticSearch(String query, String conversationId) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(10)                   //先返回足够多的记忆
                .similarityThreshold(0.65)
                .filterExpression("conversationId == '" + conversationId + "' and type == 'episodic'")
                .build();

        return vectorStore.similaritySearch(request);
    }

    private List<Document> rerankByDecay(List<Document> docs, int topK) {
        Instant now = Instant.now();

        return docs.stream()
                .map(doc -> {
                    double importance = Double.parseDouble(
                            doc.getMetadata().getOrDefault("importance", "0.5").toString());
                    // 修复：毫秒时间戳字符串需要转换为 long，再转为 Instant
                    long timestampMs = Long.parseLong(
                            doc.getMetadata().get("timestamp").toString());
                    Instant timestamp = Instant.ofEpochMilli(timestampMs);
                    long daysSinceCreate = ChronoUnit.DAYS.between(timestamp, now);
                    // 时间衰减公式：score = importance * e^(-λ * 天数)
                    double score = importance * Math.exp(-DECAY_LAMBDA * daysSinceCreate);
                    return new DocumentScore(doc, score);
                })
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .map(ds -> ds.document)
                .collect(Collectors.toList());
    }

    @AllArgsConstructor
    private static class DocumentScore {
        Document document;
        double score;
    }

    private Document getDocumentById(String docId) {
        return vectorStore.similaritySearch(
                SearchRequest.builder().query("").filterExpression("id == '" + docId + "'").topK(1).build()
        ).stream().findFirst().orElse(null);
    }

    public void updateAccessTime(List<Document> docs) {
        List<Document> updatedDocs = new ArrayList<>();
        // 使用毫秒时间戳字符串，保持与 timestamp 字段格式一致
        String now = String.valueOf(System.currentTimeMillis());

        for (Document doc : docs) {
            Document originalDoc = getDocumentById(doc.getId());
            if (originalDoc != null) {
                //修改元数据中的访问时间
                originalDoc.getMetadata().put("lastAccessedAt", now);
                updatedDocs.add(originalDoc);
            }
        }
        if (!updatedDocs.isEmpty()) {
            //  add，触发 upsert 更新
            vectorStore.add(updatedDocs);
        }
    }
}
