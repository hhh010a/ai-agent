package com.hjw.chatMemory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RedisChatMemory implements ChatMemory {

    private final StringRedisTemplate stringRedisTemplate;
    private final String keyPrefix = "chat:";
    private final ObjectMapper objectMapper;
    private final int maxMessages;
    private final Duration ttl;

    public RedisChatMemory(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper, int maxMessages, Duration ttl) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
        this.maxMessages = maxMessages;
        this.ttl = ttl;
    }

    public record StoredMessage(String role, String text) { }
    @Override
    public void add(String conversationId, List<Message> messages) {
        try{
            String key=keyPrefix + conversationId;
            String s = stringRedisTemplate.opsForValue().get(key);
            List<StoredMessage> storedMessageList;
            if(s==null||s.isBlank()){
                storedMessageList = new ArrayList<>();
            }else{
                storedMessageList = objectMapper.readValue(s, new TypeReference<List<StoredMessage>>() {
                });
            }
            for(Message m:messages){
                storedMessageList.add(new StoredMessage(roleOf(m),textOf(m)));
            }
            if(storedMessageList.size()>maxMessages){
                storedMessageList = storedMessageList.subList(storedMessageList.size()-maxMessages,storedMessageList.size());
            }
            stringRedisTemplate.opsForValue().set(key,objectMapper.writeValueAsString(storedMessageList),ttl);
        }catch (Exception e){
            throw new IllegalStateException("add redis error",e);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        try{
            String key=keyPrefix + conversationId;
            String s = stringRedisTemplate.opsForValue().get(key);
            if(s==null||s.isBlank()){
                return List.of();
            }
            List<StoredMessage> storedMessageList = objectMapper.readValue(s, new TypeReference<List<StoredMessage>>() {});
            return storedMessageList.stream().map(this::toMessage).collect(Collectors.toList());
        }catch (Exception e){
            throw new IllegalStateException("get redis error",e);
        }
    }

    @Override
    public void clear(String conversationId) {
        stringRedisTemplate.delete(keyPrefix + conversationId);
    }

    public String roleOf(Message message){
        MessageType messageType = message.getMessageType();
        if(messageType== MessageType.USER) return "user";
        if(messageType== MessageType.ASSISTANT) return "assistant";
        if (messageType== MessageType.SYSTEM) return "system";
        return "unknown";
    }

    public String textOf(Message message){
        return message.getText()==null?"":message.getText();
    }

    public Message toMessage(StoredMessage storedMessage){
        if(storedMessage.role().equals("user")) return new UserMessage(storedMessage.text());
        if(storedMessage.role().equals("assistant")) return new AssistantMessage(storedMessage.text());
        if(storedMessage.role().equals("system")) return new SystemMessage(storedMessage.text());
        return new UserMessage(storedMessage.text());
    }
}
