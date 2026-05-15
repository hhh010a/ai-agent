package com.hjw.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class MyFileReader {

    private static final String LOADED_FILES_KEY = "rag:loaded:files";
    private final ResourcePatternResolver resourcePatternResolver;
    private final StringRedisTemplate redisTemplate;

    public MyFileReader(ResourcePatternResolver resourcePatternResolver,
                        StringRedisTemplate redisTemplate) {
        this.resourcePatternResolver = resourcePatternResolver;
        this.redisTemplate = redisTemplate;
    }

    public List<Document> loadFiles(){
        List<Document> documents = new ArrayList<>();
        Set<String> existingFiles = getExistingFilesFromRedis();

        ExtractedTextFormatter formatter = ExtractedTextFormatter.builder()
                .withNumberOfBottomTextLinesToDelete(0)
                .withNumberOfTopTextLinesToDelete(0)
                .withNumberOfTopPagesToSkipBeforeDelete(0)
                .build();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*");

            for(Resource resource:resources){
                TikaDocumentReader reader = new TikaDocumentReader(resource, formatter);
                List<Document> documentList = reader.read();
                String originalFilename = resource.getFilename();
                if(documentList.isEmpty()){
                    log.error("空列表");
                    continue;
                }
                if (existingFiles.contains(originalFilename)) {
                    log.debug("跳过已加载的文件: {}", originalFilename);
                    continue;
                }
                for(Document document:documentList){
                    Map<String, Object> extraMetadata = new HashMap<>();
                    extraMetadata.put("source_filename", originalFilename);
                    document.getMetadata().putAll(extraMetadata);
                    if (document.getText() == null || document.getText().isBlank()) {
                        log.warn("文件 {} 解析结果为空", originalFilename);
                        continue;
                    }
                    documents.add(document);
                }
                markFileAsLoaded(originalFilename);
            }
        } catch (Exception e) {
            log.error("文件解析错误", e);
        }
        return documents;
    }

    private Set<String> getExistingFilesFromRedis() {
        try {
            Set<String> members = redisTemplate.opsForSet().members(LOADED_FILES_KEY);
            return members != null ? members : Collections.emptySet();
        } catch (Exception e) {
            log.warn("从Redis获取已加载文件失败", e);
            return Collections.emptySet();
        }
    }

    private void markFileAsLoaded(String filename) {
        try {
            redisTemplate.opsForSet().add(LOADED_FILES_KEY, filename);
        } catch (Exception e) {
            log.warn("标记文件为已加载失败: {}", filename, e);
        }
    }

    /**
     * 清除已加载文件记录（用于重新加载）
     */
    public void clearLoadedFilesCache() {
        redisTemplate.delete(LOADED_FILES_KEY);
    }
}
