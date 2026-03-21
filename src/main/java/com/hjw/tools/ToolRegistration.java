package com.hjw.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Bean
    public ToolCallback[] registerTools(){
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        WebSearchTool webSearchTool = new WebSearchTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        ToolCallback[] tools = ToolCallbacks.from(
                fileOperationTool,
                webScrapingTool,
                resourceDownloadTool,
                webSearchTool,
                pdfGenerationTool);
        return tools;
    }
}
