package com.demo.dev;

import com.okreduce.core.AbstractParser;
import com.okreduce.core.AndroidExecutorDelivery;
import com.okreduce.core.OkCallback;
import com.okreduce.core.ApiConfigBuilder;
import com.okreduce.core.AsyncExecutorDelivery;
import com.okreduce.core.OkConfig;
import com.okreduce.core.ExecutorDelivery;
import com.okreduce.OKHttpExecutor;
import com.okreduce.constant.ContentType;
import com.okreduce.constant.Method;
import com.demo.bean.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.okreduce.core.OkRequest;
import com.okreduce.util.StreamUtil;
import com.okreduce.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class Login {
    private final static String URL = "http://localhost:8080/";
    private final static String[] BACKUP_URLS = {"http://localhost:8080/"};
    private final static String URI = "user/login";
    private final static String LOG = "用户登录";

    private final static Method METHOD = null;
    private final static ContentType CONTENT_TYPE = null;
    private final static Boolean DEBUG = null;

    private final static int CONNECT_TIMEOUT = 1000;
    private final static int READ_TIMEOUT = 1000;
    private final static int WRITE_TIMEOUT = 1000;
    private final static int RETRY_COUNT = 1;

    private OkConfig config;
    private String uri = URI;

    private ExecutorDelivery delivery;
    private Object entity;

    private Map<String, String> headerMap = new HashMap<>();
    private Map<String, Object> paramMap = new HashMap<>();

    public Login(OkConfig config) {
        this.config = config;
        initialValue();
    }

    private void initialValue(){
        paramMap.put("name","shasha");
    }

    public final static OkApiConfigBuilder config() {
        return new OkApiConfigBuilder();
    }

    public final static Login api() {
        return new OkApiConfigBuilder().api();
    }

    public final static Login api(OkConfig config) {
        return new Login(config);
    }

    public Login fill(String name, String password) {
        this.name(name);
        this.password(password);
        return this;
    }

    public Login uri(String uri) {
        this.uri = uri;
        return this;
    }

    public Login header(String key, String value) {
        headerMap.put(key, value);
        return this;
    }


    public Login param(String key, Object value) {
        paramMap.put(key, value);
        return this;
    }

    public Login user(User user) {
        this.entity = user;
        return this;
    }

    //仅Android生成此方法
    public Login inThread() {
        delivery = new AsyncExecutorDelivery();
        return this;
    }

    public Login name(String value) {
        if(value!=null){
            paramMap.put("name", value);
        }
        return this;
    }

    public Login password(String value) {
        paramMap.put("password", value);
        return this;
    }

    public Response origExecute() throws Exception {
        OkRequest request=new OkRequest(config, uri, headerMap, paramMap, entity);
        OKHttpExecutor executor = new OKHttpExecutor(request);
        return executor.origExecute();
    }

    public void origEnqueue(okhttp3.Callback callBack) {
        OkRequest request=new OkRequest(config, uri, headerMap, paramMap, entity);
        OKHttpExecutor executor = new OKHttpExecutor(request);
        executor.origEnqueue(callBack);
    }

    public User execute() throws Exception {
        OkRequest request=new OkRequest(config, uri, headerMap, paramMap, entity);
        OKHttpExecutor executor = new OKHttpExecutor(request);
        return (User) executor.execute();
    }

    public void enqueue(OkCallback callBack) {
        OkRequest request=new OkRequest(config, uri, headerMap, paramMap, entity);
        ExecutorDelivery delivery = this.delivery != null ? this.delivery : new AndroidExecutorDelivery();
        OKHttpExecutor executor = new OKHttpExecutor(request, delivery);
        executor.enqueue(callBack);
    }

    public static class OkApiConfigBuilder extends ApiConfigBuilder<OkApiConfigBuilder> {
        protected OkApiConfigBuilder() {
            super(URL, BACKUP_URLS, METHOD, CONTENT_TYPE, CONNECT_TIMEOUT, READ_TIMEOUT,
                    WRITE_TIMEOUT, RETRY_COUNT, new OkApiParser(),DEBUG, LOG);
        }

        @Override
        public Login api() {
            return new Login(build());
        }
    }

    public static class OkApiParser extends AbstractParser {
        @Override
        public String toJson(OkRequest request, Object entity) throws Exception {
            return new Gson().toJson(entity);
        }

        @Override
        public Object toEntity(OkRequest request, InputStream input) throws Exception {
            Gson gson = new GsonBuilder().create();
            return gson.fromJson(StreamUtil.readString(input), User.class);
        }
    }

    //
//    private static final class UserListGsonBodyConverter extends AbstractParser {
//        @Override
//        public String toJson(OkRequest request, Object entity) throws Exception {
//            return new Gson().toJson(entity);
//        }
//
//        @Override
//        public Object toEntity(OkRequest request, InputStream input) throws Exception {
//            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
//            return gson.fromJson(StreamUtil.readString(input), new TypeToken<List<User>>() {
//            }.getType());
//        }
//    }
//
//
//    private final class StringBodyConverter extends AbstractParser {
//        @Override
//        public String toJson(OkRequest request, Object entity) throws Exception {
//            return new Gson().toJson(entity);
//        }
//
//        @Override
//        public Object toEntity(OkRequest request,  InputStream input) throws Exception {
//            return StreamUtil.readString(input);
//        }
//    }
//
//    private static class FileBodyParser extends AbstractParser {
//        @Override
//        public String toJson(OkRequest request, Object entity) throws Exception {
//            return new Gson().toJson(entity);
//        }
//
//        @Override
        public Object toEntity(OkRequest request, InputStream input) throws Exception {
            File saveFile;
            if (request.getConfig().getSavePath() != null) {
                saveFile = new File(request.getConfig().getSavePath(), Util.getUUID());
            } else {
                saveFile = File.createTempFile(Util.getUUID(), "");
            }
            if(!saveFile.getParentFile().exists()){
                saveFile.getParentFile().mkdirs();
            }
            FileOutputStream outputStream = new FileOutputStream(saveFile);
            StreamUtil.outputStream(input, outputStream);
            return saveFile;
        }
//    }
//
//    private final class ByteBodyConverter extends AbstractParser {
//        @Override
//        public String toJson(OkRequest request, Object entity) throws Exception {
//            return new Gson().toJson(entity);
//        }
//
//        @Override
//        public Object toEntity(OkRequest request, InputStream input) throws Exception {
//            return StreamUtil.readStream(input);
//        }
//    }

}
