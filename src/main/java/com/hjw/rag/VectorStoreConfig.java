package com.hjw.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class VectorStoreConfig {

    @Resource
    private MyMarkdownReader myMarkdownReader;

    @Resource
    MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore codeGuideVectorStore(VectorStore vectorStore){
        List<Document> documents = myMarkdownReader.loadMarkdowns();
        for(int i=0;i<documents.size();i+=10){
            int endIndex=Math.min(i + 10, documents.size());
            List<Document> documents1 = documents.subList(i, endIndex);
            documents1 = myKeywordEnricher.enrichDocuments(documents1);
            vectorStore.add( documents1);
        }
        return vectorStore;
    }
}
