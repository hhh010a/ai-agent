package com.hjw.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class MyKeywordEnricher {

    @Resource
    private  ChatModel dashScopeChatModel;
    List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(dashScopeChatModel)
                .keywordCount(5)
                .build();


        return enricher.apply(documents);
    }
}
