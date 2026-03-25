package com.hjw.tools;

import org.jsoup.Jsoup;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;


public class WebScrapingTool {

    @Tool(description = "Crawl the content of the specified webpage and extract information such as the page title and body text ")
    public String webScraping(@ToolParam(description = "The complete URL of the webpage to be crawled") String url){
        try {
            return Jsoup.connect( url).get().toString();
        } catch (Exception e) {
            return "Error web scraping"+ e.getMessage();
        }
    }
}
