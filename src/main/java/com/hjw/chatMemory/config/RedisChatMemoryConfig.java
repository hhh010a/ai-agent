package com.hjw.chatMemory.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hjw.chatMemory.RedisChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Configuration
public class RedisChatMemoryConfig {

    @Bean
    public ChatMemory redisChatMemory(StringRedisTemplate stringRedisTemplate,
                                      ObjectMapper objectMapper,
                                      @Value("${chat.memory.max-messages}") int maxMessages,
                                      @Value("${chat.memory.ttl}") Duration ttl){
        return new RedisChatMemory(stringRedisTemplate,objectMapper,maxMessages,ttl);
    }
}
