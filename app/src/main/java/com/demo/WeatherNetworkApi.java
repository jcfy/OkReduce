package com.demo;

import com.demo.bean.Weather;
import com.demo.bean.WeatherResponse;
import com.okreduce.annotation.OkConfig;
import com.okreduce.annotation.OkGet;
import com.okreduce.annotation.OkParam;
import com.okreduce.annotation.OkPost;
import com.okreduce.constant.ContentType;

import java.util.List;

@OkGet(url = "http://www.test.com/",urls = {"http://api.k780.com/"},type = ContentType.FORM)
@OkConfig(debug = true)
public interface WeatherNetworkApi {
    @OkConfig(log = "获取实时天气")
    WeatherResponse<Weather> todayWeather(@OkParam(initial = "1") int weaid, @OkParam(initial = "weather.today") String app,
                                                 @OkParam(initial = "json") String format);
    @OkPost
    @OkConfig(log = "获取天气预报(5-7天)")
    WeatherResponse<List<Weather>> futureWeather(@OkParam(initial = "1") int weaid, @OkParam(initial = "weather.future") String app,
                                                 @OkParam(initial = "json") String format);
}
