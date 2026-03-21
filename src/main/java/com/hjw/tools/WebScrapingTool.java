package com.hjw.tools;

import org.jsoup.Jsoup;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;


public class WebScrapingTool {

    @Tool(description = "Web scraping ")
    public String webScraping(@ToolParam(description = "URL to scrape") String url){
        try {
            return Jsoup.connect( url).get().toString();
        } catch (Exception e) {
            return "Error web scraping"+ e.getMessage();
        }
    }
}
