package com.hjw.agent;

import cn.hutool.core.util.StrUtil;
import com.hjw.agent.module.State;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public abstract class BaseAgent {

    private String agentName;

    private String systemPrompt;
    private String nextStepPrompt;

    private State state=State.IDLE;

    private ChatClient chatClient;

    private List<Message> messageList=new ArrayList<>();

    private int MaxStep=10;
    private int currentStep=0;

    public String run(String userPrompt){
        if(state!= State.IDLE){
            throw new RuntimeException("Can't start running with this state:"+this.state);
        }
        if(StrUtil.isBlank(userPrompt)){
            throw new RuntimeException("userPrompt can't be empty");
        }
        state= State.RUNNING;
        messageList.add(new UserMessage(userPrompt));
        List<String> results=new ArrayList<>();

        try {
            for(int i=0;i<MaxStep&&state!=State.FINISHED;i++){
                currentStep=i;
                String result = step();
                log.info("step"+currentStep+":"+result);
                results.add(result);
            }
            if(currentStep>=MaxStep){
                state= State.FINISHED;
                results.add("MaxStep reached :"+MaxStep);
            }
            return StrUtil.join("\n",results);
        } catch (Exception e) {
            state= State.ERROR;
            log.error("Error in agent:"+agentName,e);
            return "Error in agent:"+agentName+"\n"+e.getMessage();
        }
    }

    public abstract String step();
}
