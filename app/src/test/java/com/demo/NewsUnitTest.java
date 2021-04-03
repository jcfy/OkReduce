package com.demo;


import com.demo.bean.News;
import com.demo.bean.Result;
import com.okreduce.api.Download;
import com.okreduce.api.NewsList;
import com.okreduce.api.Register;
import com.okreduce.api.UploadFile;
import com.okreduce.core.OkCallback;
import com.okreduce.core.OkGlobalConfig;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Interceptor;
import okhttp3.Response;

public class NewsUnitTest {
    public NewsUnitTest(){
        OkGlobalConfig.builder().url("http://localhost:8080/","http://localhost2:8080/")
                .debug(true)
                .interceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        return null;
                    }
                })
                .savePath(new File("E:\\OkDownload"));
        try {
            OkGlobalConfig.builder().savePath(new File("E:\\OkDownload"));
//            OkGlobalConfig.loadConfig("D:\\Project\\OkHttpAnno-master\\app\\src\\main\\java\\com\\demo\\okconfig.properties");
//            OkGlobalConfig.builder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRegister(){
        try {
            Result<List<News>> result= NewsList.api().execute();
            System.out.println("========"+result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRegister2(){
        try {
            NewsList.api().enqueue(new OkCallback<Result<List<News>>>() {
                @Override
                public void onSucceed(Result<List<News>> entity) {

                }

                @Override
                public void onError(int code, String errorBody, Exception e) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDownload(){
        try {
            File file= Download.api().execute();
            System.out.println("========"+file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUploadFile(){
        try {
            String id= UploadFile.api().id("123456")
                    .file1(new File("C:\\Users\\Andy\\Desktop\\使用场景.txt"))
                    .file2(new File("C:\\Users\\Andy\\Desktop\\iphone6.txt"))
                    .execute();
            System.out.println("========"+id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
