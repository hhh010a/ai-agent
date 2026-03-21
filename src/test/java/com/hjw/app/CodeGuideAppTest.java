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
}
