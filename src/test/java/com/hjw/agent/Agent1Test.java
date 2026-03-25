package com.hjw.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class Agent1Test {

    @Resource
    private Agent1 agent1;

    @Test
    public void testRun() {
        String userPrompt = """  
                我现在在北京天安门 给我一个观光地点 
                并结合一些网络图片，制定一份详细的计划，  
                并以 PDF 格式输出""";
        String answer = agent1.run(userPrompt);
        System.out.println(answer);
    }
}
