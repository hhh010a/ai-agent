package com.hjw.tools;

import cn.hutool.json.JSONObject;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;


public class WebSearchTool {

    private final String key;

    public WebSearchTool(String key) {
        this.key = key;
    }

    @Tool(description = "Web search tool")
    public String search(@ToolParam(description = "Query to search for") String query) {
        try {
            OkHttpClient client = new OkHttpClient();

            HttpUrl.Builder urlBuilder = HttpUrl.get("https://www.searchapi.io/api/v1/search").newBuilder();
            urlBuilder.addQueryParameter("engine", "baidu");
            urlBuilder.addQueryParameter("q", query);
            urlBuilder.addQueryParameter("api_key", key);

            Request request = new Request.Builder()
                    .url(urlBuilder.build())
                    .build();

            Response response = client.newCall(request).execute();
            String jsonStr = response.body().string();

            JSONObject jsonObject = new JSONObject(jsonStr);
            StringBuilder result = new StringBuilder();

            if (jsonObject.containsKey("organic_results")) {
                var organicResults = jsonObject.getJSONArray("organic_results");
                int count = Math.min(organicResults.size(), 5);
                for (int i = 0; i < count; i++) {
                    JSONObject item = organicResults.getJSONObject(i);
                    String title = item.getStr("title", "");
                    String link = item.getStr("link", "");
                    String snippet = item.getStr("snippet", "");
                    result.append(i + 1).append(". ").append(title).append("\n");
                    result.append("   链接: ").append(link).append("\n");
                    result.append("   摘要: ").append(snippet).append("\n\n");
                }
            }

            if (result.length() == 0) {
                return "未找到相关结果";
            }

            return result.toString().trim();
        } catch (Exception e) {
            return "搜索出错: " + e.getMessage();
        }
    }
}