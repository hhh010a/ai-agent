package com.hjw.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
class MyMarkdownReader {

    @jakarta.annotation.Resource
    private VectorStore vectorStore;

    private final ResourcePatternResolver resourcePatternResolver;

    public MyMarkdownReader(ResourcePatternResolver resourcePatternResolver
            , VectorStore vectorStore) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.vectorStore = vectorStore;
    }
    public List<Document> loadMarkdowns() {
        List<Document> documents = new ArrayList<>();
        Set<String> existingFileNames = existingMarkdowns();
        try{
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            String fileName;
            String level;
            for (Resource resource : resources){
                fileName = resource.getFilename();
                if (existingFileNames.contains(fileName)){
                    continue;
                }
                level = fileName.substring(fileName.length()-7,fileName.length()-5);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", fileName)
                        .withAdditionalMetadata("level",level)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                documents.addAll(reader.get());
            }
        }catch (Exception e){
            log.error("load markdown error",e);
        }
        return documents;
    }
    public Set< String> existingMarkdowns(){
        List<Document> documents = vectorStore.similaritySearch("*");
        if(documents.isEmpty()){
            return Collections.emptySet();
        }
        return documents.stream()
                .map(document -> document.getMetadata().get("filename"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
