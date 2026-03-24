package com.hjw.tool;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;



@Component
public class WeatherQueryTool {


    private static final String API_URL = "https://uapis.cn/api/v1/misc/weather";

    @Tool(description = "Query real-time weather information for a city including temperature, humidity, wind direction and weather condition")
    public String queryWeather(@ToolParam(description = "City name or district name to query, e.g., 北京, 上海, 新余市, 渝水区") String city) {
        try {
            // 构建请求URL，添加城市参数
            String url = API_URL + "?city=" + city;

            // 使用Hutool发送GET请求
            HttpResponse response = HttpRequest.get(url)
                    .timeout(5000)  // 设置超时时间5秒
                    .execute();

            // 获取响应体
            String body = response.body();

            // 解析JSON响应
            JSONObject jsonObject = JSONUtil.parseObj(body);

            // 检查必要字段是否存在
            if (!jsonObject.containsKey("weather")) {
                return "Query failed: Unable to get weather information for " + city;
            }

            // 提取数据
            StringBuilder weatherInfo = new StringBuilder();
            weatherInfo.append("【").append(jsonObject.getStr("city", city)).append("天气实况】\n\n");

            // 基本天气信息
            weatherInfo.append("天气状况: ").append(jsonObject.getStr("weather")).append("\n");
            weatherInfo.append("温度: ").append(jsonObject.getInt("temperature", 0)).append("℃\n");
            weatherInfo.append("湿度: ").append(jsonObject.getInt("humidity", 0)).append("%\n");
            weatherInfo.append("风向: ").append(jsonObject.getStr("wind_direction", "未知")).append("\n");
            weatherInfo.append("风力: ").append(jsonObject.getStr("wind_power", "未知")).append("\n");

            // 地理位置信息
            String province = jsonObject.getStr("province", "");
            String district = jsonObject.getStr("district", "");
            if (!province.isEmpty() || !district.isEmpty()) {
                weatherInfo.append("\n【位置信息】\n");
                if (!province.isEmpty()) {
                    weatherInfo.append("省份: ").append(province).append("\n");
                }
                if (!district.isEmpty()) {
                    weatherInfo.append("区县: ").append(district).append("\n");
                }
            }

            // 行政区划代码
            String adcode = jsonObject.getStr("adcode", "");
            if (!adcode.isEmpty()) {
                weatherInfo.append("行政区划代码: ").append(adcode).append("\n");
            }

            // 数据更新时间
            String reportTime = jsonObject.getStr("report_time", "");
            if (!reportTime.isEmpty()) {
                weatherInfo.append("\n数据更新时间: ").append(reportTime);
            }

            return weatherInfo.toString();

        } catch (Exception e) {
            return "Error querying weather: " + e.getMessage();
        }
    }

    @Tool(description = "Query simple weather information for a city, return brief result")
    public String querySimpleWeather(@ToolParam(description = "City name to query") String city) {
        try {
            String url = API_URL + "?city=" + city;

            HttpResponse response = HttpRequest.get(url)
                    .timeout(5000)
                    .execute();

            String body = response.body();
            JSONObject jsonObject = JSONUtil.parseObj(body);

            // 简洁格式返回
            String cityName = jsonObject.getStr("city", city);
            String weather = jsonObject.getStr("weather", "未知");
            int temperature = jsonObject.getInt("temperature", 0);
            String windDirection = jsonObject.getStr("wind_direction", "未知");
            String windPower = jsonObject.getStr("wind_power", "未知");

            return String.format("%s当前天气：%s，温度%d℃，%s%s",
                    cityName, weather, temperature, windDirection, windPower);

        } catch (Exception e) {
            return "Error querying weather: " + e.getMessage();
        }
    }

    @Tool(description = "Query weather and return raw JSON response")
    public String queryWeatherRaw(@ToolParam(description = "City name to query") String city) {
        try {
            String url = API_URL + "?city=" + city;

            HttpResponse response = HttpRequest.get(url)
                    .timeout(5000)
                    .execute();

            return response.body();

        } catch (Exception e) {
            return "Error querying weather: " + e.getMessage();
        }
    }

}
