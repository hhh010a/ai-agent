package com.hjw.agent;

import com.hjw.advisor.LoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

@Component
public class Agent1 extends ToolCallAgent {
    public Agent1(ToolCallback[] availableTools, ChatModel dashScopeChatModel) {
        super(availableTools);
        setAgentName("Agent1");
//        setSystemPrompt("""
//                You are Agent1, an all-capable AI assistant, aimed at solving any task presented by the user.
//                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
//                If you want to stop the interaction at any point, use the `terminate` tool.
//                If you think the problem is simple(such as simple greetings), use `terminate` tool to quit.
//                """);
        setSystemPrompt(""" 
                You are Agent1, an elite AI assistant with access to various tools including `terminate`. 
                ## Core Rules
                1. **Every step must involve an action**: either invoking a tool or calling terminate to end the conversation
                2. **Do not output "No need to call tool"**: When you believe no other tools are required, **you must call the terminate tool to end the conversation**
                3. **terminate is the only way to end the conversation**: do not output text to try to end it; you must call terminate
                ## Workflow
                1. Consider whether it is necessary to use a tool
                2. If necessary → Use the corresponding tool → Continue based on the result
                3. If not needed → **Immediately call the terminate tool**
                ## Incorrect Demonstration
                - "No need to call the tool" → Wrong! You must call terminate
                ## Correct demonstration
                - Deems no tool necessary → Invokes `terminate` → Conversation ends
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
