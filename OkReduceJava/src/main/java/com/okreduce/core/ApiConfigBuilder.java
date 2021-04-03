package com.okreduce.core;

import com.okreduce.constant.ContentType;
import com.okreduce.constant.Method;

import java.io.File;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.Interceptor;

public abstract class ApiConfigBuilder<T> {
    private OkConfig.Builder builderDelegate;

    protected ApiConfigBuilder(String url, String[] backupUrls, Method method, ContentType contentType,
                               int connectTimeout, int readTimeout, int writeTimeout, int retryCount, IParser parser, Boolean debug, String log) {
        OkConfig.Builder builder = OkGlobalConfig.builder();
        if (builder == null) {
            builder = OkGlobalConfig.newBuilder();
        }
        builderDelegate = (OkConfig.Builder) builder.clone();
        if (builderDelegate == null) {
            builderDelegate = OkGlobalConfig.newBuilder();
        }
        init(url, backupUrls, method, contentType, connectTimeout, readTimeout, writeTimeout, retryCount, parser, debug, log);
    }

    private void init(String url, String[] backupUrls, Method method, ContentType type,
                      int connectTimeout, int readTimeout, int writeTimeout, int retryCount, IParser parser, Boolean debug, String log) {
        if (url != null && !url.isEmpty() && backupUrls != null && backupUrls.length > 0) {
            builderDelegate.url(url, backupUrls);
        } else if (url != null && !url.isEmpty()) {
            builderDelegate.url(url);
        }

        if (method != null) {
            builderDelegate.method(method);
        }

        if (type != null) {
            builderDelegate.type(type);
        }

        if (connectTimeout > 0) {
            builderDelegate.connectTimeout(connectTimeout);
        }

        if (readTimeout > 0) {
            builderDelegate.readTimeout(readTimeout);
        }

        if (writeTimeout > 0) {
            builderDelegate.writeTimeout(writeTimeout);
        }

        if (retryCount > 0) {
            builderDelegate.retryCount(retryCount);
        }

        if (builderDelegate.parser == null) {
            builderDelegate.parser(parser);
        }

        if (debug != null) {
            builderDelegate.debug(debug);
        }

        if (log != null && !log.isEmpty()) {
            builderDelegate.log(log);
        }
    }

    public T url(String url, String... backupUrls) {
        builderDelegate.url(url, backupUrls);
        return (T) this;
    }

    public T networkInterceptor(Interceptor interceptor) {
        builderDelegate.networkInterceptor(interceptor);
        return (T) this;
    }

    public T interceptor(Interceptor interceptor) {
        builderDelegate.interceptor(interceptor);
        return (T) this;
    }

    public T ssl(SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier) {
        builderDelegate.ssl(sslSocketFactory, hostnameVerifier);
        return (T) this;
    }

    public T connectTimeout(int timeout) {
        builderDelegate.connectTimeout(timeout);
        return (T) this;
    }

    public T readTimeout(int timeout) {
        builderDelegate.readTimeout(timeout);
        return (T) this;
    }

    public T writeTimeout(int timeout) {
        builderDelegate.writeTimeout(timeout);
        return (T) this;
    }

    public T retryCount(int count) {
        builderDelegate.retryCount(count);
        return (T) this;
    }

    public T savePath(File file) {
        builderDelegate.savePath(file);
        return (T) this;
    }

    public T method(Method method) {
        builderDelegate.method(method);
        return (T) this;
    }

    public T type(ContentType type) {
        builderDelegate.type(type);
        return (T) this;
    }

    public T header(Map<String, String> map) {
        builderDelegate.header(map);
        return (T) this;
    }

    public T param(Map<String, Object> map) {
        builderDelegate.param(map);
        return (T) this;
    }

    public T debug(Boolean debug) {
        builderDelegate.debug(debug);
        return (T) this;
    }

    public T log(String log) {
        builderDelegate.log(log);
        return (T) this;
    }

    protected OkConfig build() {
        return builderDelegate.build();
    }

    public abstract Object api();
}
