package com.hjw.app;

import com.hjw.advisor.LoggerAdvisor;
import com.hjw.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CodeGuideApp {


    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT= """
            你是一位资深Java编程导师，以引导式教学为核心：先通过提问深入了解用户的思路、已尝试方法和遇到的困难，
            再逐步提供线索和原理，鼓励用户自己发现解决方案；根据用户水平调整引导深度，用积极语言激发探索欲，
            通过复述确认理解，适时给出简洁示例启发，但从不直接给完整答案；始终耐心倾听每个问题，并在对话中主动询问反馈，
            旨在培养用户的独立编程思维和能力。
            回复精简
            """;

    @Resource
    private VectorStore codeGuideVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    //创建chatclient
    public CodeGuideApp(ChatModel  dashscopeChatModel, ChatMemory redisChatMemory){
        chatClient =ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(redisChatMemory).build(),
                        new LoggerAdvisor()
                )
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public String chat(String userInput ,String chatId){
        userInput = queryRewriter.rewrite(userInput);
        ChatResponse chatResponse = chatClient.prompt()
                .user(userInput)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }
    record CodeReport(String title, List<String> suggestions){}
    public CodeReport chatWithReport(String userInput ,String chatId){
        userInput = queryRewriter.rewrite(userInput);
        CodeReport codeReport = chatClient.prompt()
                .user(userInput)
                .system(SYSTEM_PROMPT + "对话结束后 生成一个报告 标题为{用户名}的编程报告 内容为编程学习建议列表")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(CodeReport.class);
        log.info("CodeReport: {}",codeReport);
        return codeReport;
    }

    public String chatWithRag(String userInput ,String chatId){
        userInput = queryRewriter.rewrite(userInput);
        ChatResponse chatResponse = chatClient.prompt()
                .user(userInput)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(RetrievalAugmentationAdvisor.builder() //自定义RagAdvisor
                        .documentRetriever( VectorStoreDocumentRetriever //设置文档检索器
                                .builder()
                                .vectorStore(codeGuideVectorStore) //设置向量数据库
                                .filterExpression(new FilterExpressionBuilder() //设置过滤器
                                        .eq("level", "高级") //
                                        .build())
                                .build() )
                        .queryAugmenter(ContextualQueryAugmenter.builder()
                                .allowEmptyContext(true)  //允许上下文为空 检索为空也能回答
                                .build())
                        .build())
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

}
