package com.hjw.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void webScraping() {
        WebScrapingTool webScrapingTool=new WebScrapingTool();
        System.out.println(webScrapingTool.webScraping("https://www.bilibili.com/"));
    }
}
