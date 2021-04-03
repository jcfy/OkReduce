package com.okreduce;

import com.okreduce.core.OkCallback;
import com.okreduce.util.Log;
import com.okreduce.core.OkConfig;
import com.okreduce.core.ExecutorDelivery;
import com.okreduce.core.IParser;
import com.okreduce.core.OkRequest;
import com.okreduce.core.ResponseException;
import com.okreduce.core.RetryPolicy;
import com.okreduce.util.CommonUtil;
import com.okreduce.util.MapUtil;
import com.okreduce.constant.ContentType;
import com.okreduce.constant.Method;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OKHttpExecutor {
    private OkHttpClient client;
    private OkRequest request;
    private OkConfig config;
    private String[] urls;
    private ExecutorDelivery delivery;
    private Log log;

    public OKHttpExecutor(OkRequest request) {
        this(request, null);
    }

    public OKHttpExecutor(OkRequest request, ExecutorDelivery delivery) {
        this.request = request;
        this.delivery = delivery;
        config = request.getConfig();
        urls = config.getFillUrls();
        client = createOkHttpClient(config);
        log = new Log(config.getDebug());
        checkRequestParam();
    }

    private OkHttpClient createOkHttpClient(OkConfig config) {
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        if (config.getConnectTimeout() > 0) {
            okHttpBuilder.connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS);
        }
        if (config.getReadTimeout() > 0) {
            okHttpBuilder.readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS);
        }
        if (config.getWriteTimeout() > 0) {
            okHttpBuilder.writeTimeout(config.getWriteTimeout(), TimeUnit.MILLISECONDS);
        }
        SSLSocketFactory sslSocketFactory = config.getSslSocketFactory();
        if (sslSocketFactory != null) {
            okHttpBuilder.sslSocketFactory(sslSocketFactory);
            okHttpBuilder.hostnameVerifier(config.getHostnameVerifier());
        }
        for (Interceptor interceptor : config.getNetworkInterceptorList()) {
            okHttpBuilder.addNetworkInterceptor(interceptor);
        }

        for (Interceptor interceptor : config.getInterceptorList()) {
            okHttpBuilder.addInterceptor(interceptor);
        }
        return okHttpBuilder.build();
    }

    public Response origExecute() throws Exception {
        return (Response) performRequest(true);
    }

    public void origEnqueue(final okhttp3.Callback callBack) {
        Request request = createRequest(getWebLink(0));
        RetryPolicy retryPolicy = new RetryPolicy(config.getRetryCount());
        performRequest(callBack, request, retryPolicy, 0);
    }

    public Object execute() throws Exception {
        return performRequest(false);
    }

    public void enqueue(OkCallback callBack) {
        final RetryPolicy retryPolicy = new RetryPolicy(config.getRetryCount());
        performRequest(callBack, createRequest(getWebLink(0)), retryPolicy, 0);
    }


    private Object performRequest(boolean isOriginal) throws Exception {
        int len = urls.length;
        RetryPolicy retryPolicy = new RetryPolicy(config.getRetryCount());
        while (true) {
            try {
                for (int i = 0; i < len; i++) {
                    try {
                        String url = getWebLink(i);
                        Request request = createRequest(url);
                        Call call = client.newCall(request);
                        Response response = call.execute();
                        if (isOriginal) {
                            return response;
                        }
                        return disposeResponse(response);
                    } catch (Exception e) {
                        log.warn(config.getLog(), "exception=" + e);
                        if (i == len - 1) {
                            throw e;
                        }
                        log.info(config.getLog(), "switch to the next URL....");
                    }
                }
            } catch (Exception e) {
                if (!retryPolicy.retry()) {
                    throw e;
                }
                log.info(config.getLog(), "to retry " + retryPolicy.getCurrentRetryCount() + "/" + retryPolicy.getRetryCount() + "....");
            }
        }
    }

    private void performRequest(final okhttp3.Callback callBack, final Request request,
                                final RetryPolicy retryPolicy, final int index) {
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn(config.getLog(), "exception=" + e);
                disposeRetry(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (callBack != null) {
                        callBack.onResponse(call, response);
                    }
                } catch (IOException e) {
                    log.warn(config.getLog(), "exception=" + e);
                    disposeRetry(call, e);
                }
            }

            private void disposeRetry(Call call, IOException e) {
                if (index < urls.length - 1) {
                    log.info(config.getLog(), "switch to the next URL....");
                    performRequest(callBack, createRequest(getWebLink(index + 1)), retryPolicy, index + 1);
                } else if (retryPolicy.retry()) {
                    log.info(config.getLog(), "to retry " + retryPolicy.getCurrentRetryCount() + "/" + retryPolicy.getRetryCount() + "....");
                    performRequest(callBack, createRequest(getWebLink(0)), retryPolicy, 0);
                } else if (callBack != null) {
                    callBack.onFailure(call, e);
                }
            }
        });
    }

    private void performRequest(final OkCallback callBack, final Request request,
                                final RetryPolicy retryPolicy, final int index) {
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.warn(config.getLog(), "exception=" + e);
                disposeRetry(-1, e, null);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    Object entity = disposeResponse(response);
                    delivery.sendResponse(callBack, entity);
                } catch (Exception e) {
                    log.warn(config.getLog(), "exception=" + e);
                    disposeRetry(response == null ? -1 : response.code(), e, getErrorBody(response));
                }
            }

            private void disposeRetry(int code, Exception e, String errorBody) {
                if (index < urls.length - 1) {
                    log.info(config.getLog(), "switch to the next URL....");
                    performRequest(callBack, createRequest(getWebLink(index + 1)), retryPolicy, index + 1);
                } else if (retryPolicy.retry()) {
                    log.info(config.getLog(), "to retry " + retryPolicy.getCurrentRetryCount() + "/" + retryPolicy.getRetryCount() + "....");
                    performRequest(callBack, createRequest(getWebLink(0)), retryPolicy, 0);
                } else {
                    delivery.sendError(code, errorBody, callBack, e);
                }
            }
        });
    }

    private String getWebLink(int index) {
        String[] urls = this.urls;
        if (urls == null || urls.length == 0) {
            return null;
        }
        if (index >= urls.length) {
            return null;
        }
        String uri = request.getUri();
        return urls[index] + uri;
    }

    private Object disposeResponse(Response response) throws Exception {
        int code = response == null ? -1 : response.code();
        ResponseBody body = response == null ? null : response.body();
        MediaType mediaType = body == null ? null : body.contentType();
        log.info(config.getLog(), "httpCode=" + code + "\ncontentType=" + mediaType);

        if (response == null || body == null) {
            throw new ResponseException(code, "response body is null", response);
        }

        if (code != 200 && code != 206) {
            throw new ResponseException(code, "http status code error, code=" + code, response);
        }

        Object bodyEntity;
        IParser parser = config.getParser();
        String contentType = mediaType == null ? null : mediaType.toString();
        if (contentType != null && (contentType.indexOf("json") != -1
                || contentType.indexOf("text") != -1 || contentType.indexOf("xml") != -1)) {
            String resp = body.string();
            log.info(config.getLog(), "response=" + resp);
            byte[] bys = resp != null ? resp.getBytes() : "".getBytes();
            bodyEntity = parser.toEntity(request, new ByteArrayInputStream(bys));
        } else {
            bodyEntity = parser.toEntity(request, body.byteStream());
        }
        return bodyEntity;
    }

    private String getErrorBody(Response response) {
        String errorBody = null;
        if (response != null) {
            try {
                errorBody = response.body().string();
            } catch (Exception e2) {
            }
        }
        return errorBody;
    }

    private Request createRequest(String link) {
        Map<String, Object> paramMap = request.getFillParamMap();
        RequestBody requestBody = null;
        String url = link;
        if (request.getEntity() != null) {
            String jsonParam = getJsonEntityParam();
            requestBody = RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), jsonParam);
        } else if (CommonUtil.isMultipartParam(paramMap)) {
            final MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
            multipartBuilder.setType(MultipartBody.FORM);
            MapUtil.map(paramMap, new MapUtil.MapPut() {
                @Override
                public void map(String key, Object value) {
                    if (value instanceof File) {
                        File file = (File) value;
                        RequestBody partBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                        multipartBuilder.addFormDataPart(key, file.getName(), partBody);
                    } else if (value instanceof byte[]) {
                        byte[] bys = (byte[]) value;
                        RequestBody partBody = RequestBody.create(MediaType.parse("application/octet-stream"), bys);
                        multipartBuilder.addFormDataPart(key, "data.bin", partBody);
                    } else if (value != null) {
                        multipartBuilder.addFormDataPart(key, String.valueOf(value));
                    }
                }
            });
            requestBody = multipartBuilder.build();
        } else if (config.getMethod() == Method.POST && config.getContentType() == ContentType.JSON) {
            String jsonParam = getJsonRequestParam(paramMap);
            requestBody = RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), jsonParam);
        } else if (config.getMethod() == Method.POST) {
            final FormBody.Builder formBuild = new FormBody.Builder();
            MapUtil.map(paramMap, new MapUtil.MapPut() {
                @Override
                public void map(String key, Object value) {
                    if (value != null) {
                        formBuild.add(key, String.valueOf(value));
                    }
                }
            });
            requestBody = formBuild.build();
        } else {
            url = link + (link.endsWith("?") ? "" : "?") + CommonUtil.getGetRequestParam(paramMap);
        }
        final Request.Builder builder = new Request.Builder().url(url);
        if (requestBody != null) {
            builder.post(requestBody);
        } else {
            builder.get();
        }
        Map<String, String> headerMap = request.getFillHeaderMap();
        MapUtil.map(headerMap, new MapUtil.MapPut() {
            @Override
            public void map(String key, Object value) {
                builder.addHeader(key, String.valueOf(value));
            }
        });
        log.info(config.getLog(), "link=" + link +
                "\nmethod=" + config.getMethod() + "\ncontentType=" + config.getContentType() +
                "\nparamMap=" + paramMap + "\nheaderMap=" + headerMap);
        return builder.build();
    }

    private void checkRequestParam() {
        if ((config.getUrl() == null || config.getUrl().isEmpty())
                && (config.getBackupUrls() == null || config.getBackupUrls().length == 0)) {
            throw new RuntimeException("url与backupUrls不能同时为null");
        }
        if (config.getParser() == null) {
            throw new RuntimeException("parser不能为null");
        }
    }

    private String getJsonEntityParam() {
        if (request.getEntity() == null) {
            return "{}";
        }
        String json = "{}";
        try {
            json = config.getParser().toJson(request, request.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    private String getJsonRequestParam(Map<String, Object> paramMap) {
        if (paramMap == null || paramMap.isEmpty()) {
            return "{}";
        }
        String json = "{}";
        try {
            json = config.getParser().toJson(request, paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
        //return new JSONGenerator().generate(paramMap);
    }
}
