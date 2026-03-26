package com.hjw.agent;

import cn.hutool.core.util.StrUtil;
import com.hjw.agent.module.State;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Data
public abstract class BaseAgent {

    private String agentName;

    private String systemPrompt;
    private String nextStepPrompt;

    private State state = State.IDLE;

    private ChatClient chatClient;

    private List<Message> messageList = new ArrayList<>();

    private int MaxStep = 10;
    private int currentStep = 0;

    public String run(String userPrompt) {
        if (state != State.IDLE) {
            throw new RuntimeException("Can't start running with this state:" + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("userPrompt can't be empty");
        }
        state = State.RUNNING;
        messageList.add(new UserMessage(userPrompt));
        List<String> results = new ArrayList<>();

        try {
            for (int i = 0; i < MaxStep && state != State.FINISHED; i++) {
                currentStep = i;
                String result = step();
                log.info("step" + currentStep + ":" + result);
                results.add(result);
            }
            if (currentStep >= MaxStep) {
                state = State.FINISHED;
                results.add("MaxStep reached :" + MaxStep);
            }
            return StrUtil.join("\n", results);
        } catch (Exception e) {
            state = State.ERROR;
            log.error("Error in agent:" + agentName, e);
            return "Error in agent:" + agentName + "\n" + e.getMessage();
        }
    }

    public SseEmitter runAsStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(240000L);
        try {
            if (state != State.IDLE) {
                emitter.send("无法从当前状态开始运行：" + this.state);
                emitter.complete();
                return emitter;
            }
            if (StrUtil.isBlank(userPrompt)) {
                emitter.send("用户输入不能为空");
                emitter.complete();
                return emitter;
            }
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }
        state = State.RUNNING;
        messageList.add(new UserMessage(userPrompt));
        CompletableFuture.runAsync(() -> {
            try {  //异步执行无返回值  防止堵塞
                for (int i = 1; i <= MaxStep && state != State.FINISHED; i++) {
                    currentStep = i;
                    String result = "step" + currentStep + ":" + step();
                    log.info( result);
                    emitter.send(result);
                }
                if (currentStep >= MaxStep) {
                    state = State.FINISHED;
                    emitter.send("达到最大可执行步数:" + MaxStep);
                    emitter.complete();
                }
                emitter.complete();
            } catch (Exception e) {
                state = State.ERROR;
                try {
                    emitter.send("agent发生错误:" + agentName + "\n" + e.getMessage());
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(() -> {
            this.state = State.ERROR;
            log.warn("Sse timeout");
        });

        emitter.onCompletion(() -> {
            if (this.state == State.RUNNING) {
                this.state = State.FINISHED;
            }
            log.info("Sse completed");
        });
        return emitter;
    }

    public abstract String step();
}
