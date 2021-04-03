package com.okreduce.demo;

import com.okreduce.api.FutureWeather;
import com.okreduce.api.TodayWeather;
import com.okreduce.core.OkCallback;
import com.okreduce.core.OkGlobalConfig;


import java.io.IOException;
import java.util.List;

import com.okreduce.demo.bean.Weather;
import com.okreduce.demo.bean.WeatherResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WeatherUnitTest {

    public WeatherUnitTest() {
        OkGlobalConfig.builder().param("appkey", "10003").param("sign", "b59bc3ef6191eb9f747dd4e83c99f2a4");
    }

    @Test
    public void testToday() {
        try {
            WeatherResponse<Weather> response = TodayWeather.api().execute();
            System.out.println("===============" + response);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    @Test
    public void testFuture() {
        FutureWeather.api().enqueue(new OkCallback<WeatherResponse<List<Weather>>>() {
            @Override
            public void onSucceed(WeatherResponse<List<Weather>> entity) {
                System.out.println("=======onSucceed=" + entity);
            }

            @Override
            public void onError(int code, String errorBody, Exception e) {
                System.out.println("========onError=" + errorBody);
            }
        });
        ThreadUtil.sleepTestThread();
    }

    @Test
    public void testFutureOrigExecute() {
        try {
            Response response =FutureWeather.api().origExecute();
            System.out.println("=======onSucceed=" + response.body().string());
        } catch (Exception e) {
            System.out.println("=======onFailure=" + e);
            //e.printStackTrace();
        }
    }

    @Test
    public void testFutureOrigEnqueue() {
        FutureWeather.api().origEnqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("=======onFailure=" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("=======onSucceed=" + response.body().string());
            }
        });
        ThreadUtil.sleepTestThread();
    }
}
