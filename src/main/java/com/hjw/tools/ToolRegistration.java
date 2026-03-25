package com.hjw.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Value("${tool.websearch.api-key}")
    private String key;

    @Bean
    public ToolCallback[] registerTools(){
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        WebSearchTool webSearchTool = new WebSearchTool(key);
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();
        ToolCallback[] tools = ToolCallbacks.from(
                terminateTool,
                fileOperationTool,
                webScrapingTool,
                resourceDownloadTool,
                webSearchTool,
                pdfGenerationTool);
        return tools;
    }
}
