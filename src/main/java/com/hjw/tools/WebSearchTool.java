package com.hjw.tools;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


public class WebSearchTool {


    private final String key;

    public WebSearchTool(String  key){
        this.key = key;
    }

    @Tool(description = "Web search tool")
    public String search(@ToolParam(description = "Query to search for") String query){
        try {
            OkHttpClient client = new OkHttpClient();

            HttpUrl.Builder urlBuilder = HttpUrl.get("https://www.searchapi.io/api/v1/search").newBuilder();
            urlBuilder.addQueryParameter("engine", "baidu");
            urlBuilder.addQueryParameter("q", query);
            urlBuilder.addQueryParameter("api_key", key);

            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .build();

            Response response = null;
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            return "Error web search"+ e.getMessage();
        }
    }
}
