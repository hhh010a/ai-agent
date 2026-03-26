package com.hjw.agent;

import com.hjw.advisor.LoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class Agent1 extends ToolCallAgent{
    public Agent1(ToolCallback[] availableTools, ChatModel dashScopeChatModel){
        super(availableTools);
        setAgentName("Agent1");
        setSystemPrompt("""  
                You are Agent1, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                If you think the problem is simple, don't use a tool(such as simple greetings).
                """);
//        setNextStepPrompt("""
//                Based on user needs, proactively select the most appropriate tool or combination of tools.
//                For complex tasks, you can break down the problem and use different tools step by step to solve it.
//                After using each tool, clearly explain the execution results and suggest the next steps.
//                If you want to stop the interaction at any point, use the `terminate` tool/function call.
//                """);
        setChatClient(ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new LoggerAdvisor())
                .build());
    }
}
