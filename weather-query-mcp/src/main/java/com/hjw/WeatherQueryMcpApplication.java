package com.hjw;

import com.hjw.tool.WeatherQueryTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class WeatherQueryMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherQueryMcpApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider toolCallbackProvider(WeatherQueryTool weatherQueryTool) {
        return MethodToolCallbackProvider.builder().toolObjects(weatherQueryTool).build();
    }
}
