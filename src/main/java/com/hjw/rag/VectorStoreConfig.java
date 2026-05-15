package com.hjw.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class VectorStoreConfig {


    @Resource
    MyKeywordEnricher myKeywordEnricher;

    @Resource
    private MyFileReader myFileReader;

    @Resource
    private RecursiveTextSplitter recursiveTextSplitter;

    @Bean
    VectorStore codeGuideVectorStore(VectorStore vectorStore){
        List<Document> documents = myFileReader.loadFiles();
        log.info("加载文件数量: {}", documents.size());

        List<Document> chunks = new ArrayList<>();
        for (Document doc : documents) {
            List<Document> splitChunks = recursiveTextSplitter.split(doc);
            chunks.addAll(splitChunks);
        }
        log.info("分块后数量: {}", chunks.size());

        for (int i = 0; i < chunks.size(); i += 10) {
            int endIndex = Math.min(i + 10, chunks.size());
            List<Document> batch = chunks.subList(i, endIndex);
            batch = myKeywordEnricher.enrichDocuments(batch);
            vectorStore.add(batch);
            log.info("已添加批次 {}-{}", i, endIndex);
        }
        return vectorStore;
    }
}
