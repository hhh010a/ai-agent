package com.hjw.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    public QueryRewriter(ChatModel chatModel){
        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();
    }

    public String rewrite(String prompt){
        Query query = new Query( prompt);
        Query transform = queryTransformer.transform(query);
        return transform.text();
    }

}
