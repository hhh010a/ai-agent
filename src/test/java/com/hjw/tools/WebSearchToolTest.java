package com.hjw.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WebSearchToolTest {

    @Resource
    private WebSearchTool webSearchTool;
    @Test
    void search() {
        System.out.println(webSearchTool.search("bilibili"));
    }
}
