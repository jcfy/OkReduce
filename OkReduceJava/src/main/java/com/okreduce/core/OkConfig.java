package com.okreduce.core;

import com.okreduce.constant.ContentType;
import com.okreduce.constant.Method;

import java.io.File;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;

public class OkConfig {
    private String url;
    private String[] backupUrls;
    private Method method;
    private ContentType contentType;

    private int connectTimeout;
    private int readTimeout;
    private int writeTimeout;

    private int retryCount;
    private File savePath;
    private IParser parser;

    private SSLSocketFactory sslSocketFactory;
    private HostnameVerifier hostnameVerifier;
    private List<Interceptor> networkInterceptorList;
    private List<Interceptor> interceptorList;
    private Map<String, String> headerMap;
    private Map<String, Object> paramMap;
    private Boolean debug;
    private String log;

    public OkConfig(Builder builder) {
        this.url = builder.url;
        this.backupUrls = builder.backupUrls;
        this.method = builder.method;
        this.contentType = builder.contentType;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.retryCount = builder.retryCount;
        this.savePath = builder.savePath;
        this.parser = builder.parser;
        this.sslSocketFactory = builder.sslSocketFactory;

        this.hostnameVerifier = builder.hostnameVerifier;
        this.hostnameVerifier = builder.hostnameVerifier;
        this.networkInterceptorList = builder.networkInterceptorList;
        this.interceptorList = builder.interceptorList;
        this.headerMap = builder.headerMap;
        this.paramMap = builder.paramMap;
        this.debug = builder.debug;
        this.log = builder.log;
    }

    public String getUrl() {
        return url;
    }

    public String[] getBackupUrls() {
        return backupUrls;
    }

    public String[] getFillUrls() {
        if (url == null || url.isEmpty()) {
            return backupUrls;
        }
        if (backupUrls == null || backupUrls.length == 0) {
            return new String[]{url};
        }
        String[] urls = new String[backupUrls.length + 1];
        urls[0] = url;
        System.arraycopy(backupUrls, 0, urls, 1, backupUrls.length);
        return urls;
    }

    public Method getMethod() {
        return method == null ? Method.POST : method;
    }

    public ContentType getContentType() {
        return contentType == null ? ContentType.FORM : contentType;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public File getSavePath() {
        return savePath;
    }

    public IParser getParser() {
        return parser;
    }

    public SSLSocketFactory getSslSocketFactory() {
        if (sslSocketFactory != null) {
            return sslSocketFactory;
        }
        return createSSLSocketFactory();
    }


    public HostnameVerifier getHostnameVerifier() {
        if (hostnameVerifier != null) {
            return hostnameVerifier;
        }
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    protected SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };
            sc.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }

    public List<Interceptor> getNetworkInterceptorList() {
        return networkInterceptorList;
    }

    public List<Interceptor> getInterceptorList() {
        return interceptorList;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public Boolean getDebug() {
        return debug == null ? false : true;
    }

    public String getLog() {
        return log;
    }

    public final static class Builder implements Cloneable {
        String url;
        String[] backupUrls;
        Method method;
        ContentType contentType;

        int connectTimeout;
        int readTimeout;
        int writeTimeout;
        int retryCount;
        File savePath;
        IParser parser;

        SSLSocketFactory sslSocketFactory;
        HostnameVerifier hostnameVerifier;
        List<Interceptor> networkInterceptorList=new ArrayList<>();
        List<Interceptor> interceptorList=new ArrayList<>();
        Map<String, String> headerMap = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        Boolean debug;
        String log;

        public Builder() {

        }

        public Builder parser(IParser parser) {
            this.parser = parser;
            return this;
        }

        public Builder url(String url, String... backupUrls) {
            this.url = url;
            this.backupUrls = backupUrls;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder type(ContentType type) {
            contentType = type;
            return this;
        }

        public Builder networkInterceptor(Interceptor interceptor) {
            networkInterceptorList.add(interceptor);
            return this;
        }

        public Builder interceptor(Interceptor interceptor) {
            interceptorList.add(interceptor);
            return this;
        }

        public Builder ssl(SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier) {
            this.sslSocketFactory = sslSocketFactory;
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder connectTimeout(int timeout) {
            this.connectTimeout = timeout;
            return this;
        }

        public Builder readTimeout(int timeout) {
            this.readTimeout = timeout;
            return this;
        }

        public Builder writeTimeout(int timeout) {
            this.writeTimeout = timeout;
            return this;
        }

        public Builder retryCount(int count) {
            retryCount = count;
            return this;
        }

        public Builder savePath(File file) {
            this.savePath = file;
            return this;
        }

        public Builder header(Map<String, String> map) {
            if (map != null) {
                headerMap = map;
            }
            return this;
        }

        public Builder param(Map<String, Object> map) {
            if (map != null) {
                paramMap = map;
            }
            return this;
        }

        public Builder header(String key,String value) {
            headerMap.put(key,value);
            return this;
        }

        public Builder param(String key,Object value) {
            paramMap.put(key,value);
            return this;
        }

        public Builder debug(Boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder log(String log) {
            this.log = log;
            return this;
        }

        public OkConfig build() {
            return new OkConfig(this);
        }

        @Override
        protected Object clone() {
            try {
                Builder builder = (Builder) super.clone();
                builder.networkInterceptorList = new ArrayList<>(builder.networkInterceptorList);
                builder.interceptorList = new ArrayList<>(builder.interceptorList);
                builder.headerMap = new HashMap<>(builder.headerMap);
                builder.paramMap = new HashMap<>(builder.paramMap);
                return builder;
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
