package com.okreduce.demo;

import com.okreduce.api.TodayWeather;
import com.okreduce.core.OkGlobalConfig;
import com.okreduce.demo.bean.Weather;
import com.okreduce.demo.bean.WeatherResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OkReduce的简单使用
 */
@RestController
public class OkReduceUseController {
    @RequestMapping("/getTodayWeather")
    public WeatherResponse<Weather> getTodayWeather() {
        //全局配置，应用启动或登录成功后，全局设置一次即可 （可选）
        OkGlobalConfig.builder().param("appkey", "10003").param("sign", "b59bc3ef6191eb9f747dd4e83c99f2a4");
        try {
            //请求api接口
            return TodayWeather.api().weaid(1).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
