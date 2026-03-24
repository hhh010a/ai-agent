package com.hjw.app;

import cn.hutool.core.lang.UUID;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class CodeGuideAppTest {

    @Resource
    CodeGuideApp codeGuideApp;

    @Test
    public void chatTest(){
        String id= UUID.randomUUID().toString();

        String message="你好 我是小红";
        codeGuideApp.chat(message,id);

        message="我在编程上有一些问题";
        codeGuideApp.chat(message,id);

        message="你还记得我是谁吗";
        codeGuideApp.chat(message,id);
    }

    @Test
    public void chatTest1(){
        String id= UUID.randomUUID().toString();

        String message="你好啊啊啊啊啊 你是谁哈哈哈哈哈哈哈";
        codeGuideApp.chat(message,id);

    }


    @Test
    void chatWithReport() {
        String id= UUID.randomUUID().toString();

        String message="你好 我是小明 我最近在学习springAi";
        CodeGuideApp.CodeReport codeReport = codeGuideApp.chatWithReport(message, id);
        Assertions.assertNotNull(codeReport);
    }


    @Test
    void chatWithRag() {
        String id= UUID.randomUUID().toString();
        String message="JVM内存模型是如何划分的？";
        String result= codeGuideApp.chatWithRag(message, id);
    }

    @Test
    void chatWithTools() {
        String id = UUID.randomUUID().toString();

        // 测试1: WebSearchTool - 搜索新闻
        System.out.println("=== 测试 WebSearchTool ===");
        String message1 = "今天有什么热门科技新闻吗";
        String result1 = codeGuideApp.chatWithTools(message1, id);
        System.out.println("搜索结果: " + result1);
        Assertions.assertNotNull(result1);
        Assertions.assertFalse(result1.contains("Error"));

        // 测试2: ResourceDownloadTool - 下载图片
        System.out.println("\n=== 测试 ResourceDownloadTool ===");
        String message2 = "帮我下载一张蓝天白云的图片，保存为sky.jpg";
        String result2 = codeGuideApp.chatWithTools(message2, id);
        System.out.println("下载结果: " + result2);
        Assertions.assertNotNull(result2);

        // 测试3: WebScrapingTool - 网页抓取
        System.out.println("\n=== 测试 WebScrapingTool ===");
        String message3 = "抓取https://www.bilibili.com的页面标题";
        String result3 = codeGuideApp.chatWithTools(message3, id);
        System.out.println("抓取结果: " + result3);
        Assertions.assertNotNull(result3);

        // 测试4: FileOperationTool - 文件操作
        System.out.println("\n=== 测试 FileOperationTool ===");
        String message4 = "创建一个名为test.txt的文件，内容是'Hello Spring AI'";
        String result4 = codeGuideApp.chatWithTools(message4, id);
        System.out.println("写入结果: " + result4);
        Assertions.assertNotNull(result4);

        // 测试5: PDFGenerationTool - 生成PDF
        System.out.println("\n=== 测试 PDFGenerationTool ===");
        String message5 = "生成一个PDF文件，内容是'Spring AI学习笔记'，文件名为study.pdf";
        String result5 = codeGuideApp.chatWithTools(message5, id);
        System.out.println("PDF生成结果: " + result5);
        Assertions.assertNotNull(result5);

        // 测试6: 组合工具调用 - 先搜索再生成PDF
        System.out.println("\n=== 测试组合工具调用 ===");
        String message6 = "搜索Java 21的新特性，然后将结果保存为PDF文件java21.pdf";
        String result6 = codeGuideApp.chatWithTools(message6, id);
        System.out.println("组合调用结果: " + result6);
        Assertions.assertNotNull(result6);
    }


    @Test
    void chatWithMcp() {
        String id = UUID.randomUUID().toString();
        String message = "北京今天的天气怎么样";
        String result = codeGuideApp.chatWithMcp(message, id);
        System.out.println(result);
    }
}
