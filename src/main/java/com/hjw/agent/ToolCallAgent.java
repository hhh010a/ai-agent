package com.hjw.agent;

import com.hjw.agent.module.State;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;


@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{
    private ChatOptions chatOptions;

    private ToolCallingManager toolCallingManager;

    private final ToolCallback[] availableTools;

    private ChatResponse toolCallChatResponse;

    public ToolCallAgent(ToolCallback[] availableTools){
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(availableTools)
                .internalToolExecutionEnabled(false)
                .build();

    }

    public boolean think(){
        if(getNextStepPrompt()!=null&&!getNextStepPrompt().isEmpty()){
            getMessageList().add(new UserMessage(getNextStepPrompt()));
        }
        Prompt prompt=new Prompt(getMessageList(),chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .call()
                    .chatResponse();
            this.toolCallChatResponse=chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();

            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();
            log.info("ai think: {}",assistantMessage.getText());
            String toolCallInfo = toolCalls.stream()
                    .map(toolCall -> "name:" + toolCall.name() + ", 参数：" + toolCall.arguments())
                    .collect(Collectors.joining("\n"));
            log.info("toolcall info: {}",toolCallInfo);

            if(toolCalls.isEmpty()){
                getMessageList().add(assistantMessage);
                return false;
            }else{
                return true;
            }
        } catch (Exception e) {
            getMessageList().add(new AssistantMessage("思考时遇到问题"+e.getMessage()));
            return false;
        }
    }

    public String act(){
        if(!toolCallChatResponse.hasToolCalls()){
            return "没有工具需要调用";
        }
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(new Prompt(getMessageList(), chatOptions), toolCallChatResponse);
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage =(ToolResponseMessage) toolExecutionResult.conversationHistory()
                .getLast();
        String result = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具名称：" + toolResponse.name() + ",工具结果：" + toolResponse.responseData())
                .collect(Collectors.joining("\n"));
        if(toolResponseMessage.getResponses().stream().anyMatch(toolResponse -> toolResponse.name().equals("terminate"))){
            setState(State.FINISHED);
        }
        return result;
    }
}
