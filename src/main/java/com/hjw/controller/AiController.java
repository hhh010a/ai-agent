package com.hjw.controller;

import com.hjw.agent.Agent1;
import com.hjw.app.CodeGuideApp;
import com.hjw.tools.ToolRegistration;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private CodeGuideApp codeGuideApp;

    @Resource
    private ToolCallback[] registerTools;

    @Resource
    private ChatModel dashScopeChatModel;

    @GetMapping("codeGuide/chat/sse")
    public SseEmitter codeGuideChatByStream(String userInput, String chatId){
        SseEmitter emitter = new SseEmitter(180000L);
        codeGuideApp.chatByStream(userInput, chatId)
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
        );
        return emitter;
    }

    @GetMapping("agent1/chat/sse")
    public SseEmitter agent1ChatByStream(String userInput){
        Agent1 agent1 = new Agent1(registerTools, dashScopeChatModel);
        return agent1.runAsStream(userInput);
    }
}
