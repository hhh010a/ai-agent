package com.hjw.chatMemory.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryExtractionClientConfig {

    private final ChatOptions defaultOptions = ChatOptions.builder()
            .temperature(0.1)
            .build();
    @Bean
    public ChatClient memoryExtractionClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("你是记忆提取助手。只输出要求的JSON格式。")
                .defaultOptions(defaultOptions)
                .build();
    }
}
