package com.hjw.tool;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WeatherQueryToolTest {

    @Resource
    private WeatherQueryTool weatherQueryTool;

    @Test
    void queryWeather() {
        String weather = weatherQueryTool.queryWeather("北京");
        System.out.println(weather);
    }

    @Test
    void querySimpleWeather() {
        String weather = weatherQueryTool.querySimpleWeather("北京");
        System.out.println(weather);
    }

    @Test
    void queryWeatherRaw() {
        String weather = weatherQueryTool.queryWeatherRaw("北京");
        System.out.println(weather);
    }
}

