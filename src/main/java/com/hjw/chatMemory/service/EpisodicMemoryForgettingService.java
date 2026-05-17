package com.hjw.chatMemory.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EpisodicMemoryForgettingService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${memory.forgetting.decay-lambda:0.02}")
    private double decayLambda;

    @Value("${memory.forgetting.delete-threshold:0.1}")
    private double deleteThreshold;

    @Value("${memory.forgetting.min-unaccessed-days:30}")
    private int minUnaccessedDays;

    @Value("${memory.forgetting.max-memory-count:500}")
    private int maxMemoryCount;

    public EpisodicMemoryForgettingService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "${memory.forgetting.cron:0 0 3 * * ?}")
    public void scheduledForget() {
        log.info("开始执行主动遗忘任务...");
        try {
            List<MemoryRecord> records = fetchAllEpisodicMemories();
            if (records.isEmpty()) {
                log.info("无长期记忆，跳过遗忘任务");
                return;
            }

            List<UUID> toDelete = new ArrayList<>();
            for (MemoryRecord record : records) {
                double forgetScore = calculateForgetScore(record.metadata);
                long unaccessedDays = calculateUnaccessedDays(record.metadata);

                if (forgetScore < deleteThreshold && unaccessedDays > minUnaccessedDays) {
                    toDelete.add(record.id);
                    log.debug("遗忘记忆: id={}, score={}, unaccessedDays={}",
                            record.id, String.format("%.4f", forgetScore), unaccessedDays);
                }
            }

            if (!toDelete.isEmpty()) {
                batchDelete(toDelete);
                log.info("删除低分记忆：{}条", toDelete.size());
            }

            int evicted = evictIfOverLimit();
            log.info("主动遗忘任务完成，删除{}条，淘汰{}条", toDelete.size(), evicted);
        } catch (Exception e) {
            log.error("主动遗忘任务执行失败", e);
        }
    }

    public void forceForget() {
        scheduledForget();
    }

    public List<Document> filterForgottenMemories(List<Document> docs) {
        return docs.stream()
                .filter(doc -> calculateForgetScore(doc.getMetadata()) >= deleteThreshold)
                .collect(Collectors.toList());
    }

    public double calculateForgetScore(Map<String, Object> metadata) {
        double importance = Double.parseDouble(
                metadata.getOrDefault("importance", "0.5").toString());

        long unaccessedDays = calculateUnaccessedDays(metadata);

        return importance * Math.exp(-decayLambda * unaccessedDays);
    }

    private long calculateUnaccessedDays(Map<String, Object> metadata) {
        long lastAccessedMs = Long.parseLong(
                metadata.getOrDefault("lastAccessedAt",
                        metadata.get("timestamp").toString()).toString());
        long days = ChronoUnit.DAYS.between(Instant.ofEpochMilli(lastAccessedMs), Instant.now());
        return Math.max(0, days);
    }

    private List<MemoryRecord> fetchAllEpisodicMemories() {
        return jdbcTemplate.query(
                "SELECT id, metadata FROM vector_store WHERE metadata::jsonb ->> 'type' = 'episodic'",
                new MemoryRecordRowMapper());
    }

    private static class MemoryRecord {
        UUID id;
        Map<String, Object> metadata;

        MemoryRecord(UUID id, Map<String, Object> metadata) {
            this.id = id;
            this.metadata = metadata;
        }
    }

    private class MemoryRecordRowMapper implements RowMapper<MemoryRecord> {
        @Override
        public MemoryRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            String metadataJson = rs.getString("metadata");
            Map<String, Object> metadata;
            try {
                metadata = objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.warn("解析metadata失败: {}", metadataJson, e);
                metadata = new HashMap<>();
            }
            return new MemoryRecord(id, metadata);
        }
    }

    private int evictIfOverLimit() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vector_store WHERE metadata::jsonb ->> 'type' = 'episodic'",
                Integer.class);
        if (count == null || count <= maxMemoryCount) {
            return 0;
        }

        int toEvict = count - maxMemoryCount;
        List<UUID> ids = jdbcTemplate.queryForList(
                "SELECT id FROM vector_store WHERE metadata::jsonb ->> 'type' = 'episodic' " +
                        "ORDER BY (CAST(metadata::jsonb ->> 'importance' AS FLOAT) * " +
                        "EXP(-? * EXTRACT(EPOCH FROM (NOW() - TO_TIMESTAMP(CAST(metadata::jsonb ->> 'lastAccessedAt' AS BIGINT) / 1000.0))) / 86400.0)) ASC " +
                        "LIMIT ?",
                UUID.class, decayLambda, toEvict);

        if (!ids.isEmpty()) {
            batchDelete(ids);
            log.info("记忆超限淘汰：总数={}, 限制={}, 淘汰={}", count, maxMemoryCount, toEvict);
        }
        return toEvict;
    }

    private void batchDelete(List<UUID> ids) {
        for (int i = 0; i < ids.size(); i += 10) {
            int end = Math.min(i + 10, ids.size());
            List<UUID> batch = ids.subList(i, end);
            String placeholders = batch.stream().map(u -> "?").collect(Collectors.joining(","));
            jdbcTemplate.update(
                    "DELETE FROM vector_store WHERE id IN (" + placeholders + ")",
                    batch.toArray());
        }
    }
}
