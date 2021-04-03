package com.demo;

import com.demo.bean.News;
import com.demo.bean.Result;
import com.demo.bean.User;
import com.okreduce.annotation.OkEntity;
import com.okreduce.annotation.OkGet;
import com.okreduce.annotation.OkHead;
import com.okreduce.annotation.OkConfig;
import com.okreduce.annotation.OkParam;
import com.okreduce.annotation.OkPost;
import com.okreduce.constant.ContentType;

import java.io.File;
import java.util.List;
import java.util.Map;

@OkPost(url = "http://localhost:8080/",type = ContentType.FORM,
        urls = {" http://localhost2:8080/"," http://localhost3:8080/"})
@OkConfig(connectout = 3000,readout = 3000,writeout = 3000,retry = 1,debug = true)
public interface NewsNetworkApi {
    @OkPost(uri = "auth/login",type = ContentType.JSON)
    @OkConfig(log = "用户登录")
    String login(String name, String password);

    @OkPost(uri = "auth/register")
    @OkConfig(log = "用户注册")
    void register(@OkHead String userAgent,@OkEntity User user);

    @OkPost(uri = "user/info")
    @OkConfig(log = "用户信息")
    User userDetail(@OkParam("USER_ID") String userId);

    @OkPost(uri = "user/friendList")
    @OkConfig(log = "好友列表")
    List<User> friendList();

    @OkPost(uri = "news/detail")
    @OkConfig(log = "新闻列表")
    Result<List<News>> newsList(String newsId);

    @OkPost(uri = "news/info")
    @OkConfig(log = "新闻信息")
    Map<String, Object> newsInfo(String newsId);

    @OkGet(url = "https://down.qq.com/qqweb/PCQQ/PCQQ_EXE/PCQQ2020.exe")
    @OkConfig(log = "下载文件")
    File download();

    @OkPost(uri = "api/app/common/uploadFile")
    @OkConfig(log = "上传文件")
    String uploadFile(String id,File file1,File file2);
}
