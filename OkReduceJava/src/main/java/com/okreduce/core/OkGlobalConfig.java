package com.okreduce.core;

import java.io.FileInputStream;
import java.util.Properties;

public class OkGlobalConfig {
    private static OkConfig.Builder builder = new OkConfig.Builder();

    public static final OkConfig.Builder builder() {
        if (builder == null) {
            synchronized (OkGlobalConfig.class){
                if (builder == null) {
                    builder=new OkConfig.Builder();
                }
            }
        }
        return builder;
    }

    public synchronized static final OkConfig.Builder newBuilder() {
        OkGlobalConfig.builder = new OkConfig.Builder();
        return builder;
    }

    public static final void loadConfig(String propertiesFile) throws Exception {
        Properties properties = new Properties();
        FileInputStream inputStream = new FileInputStream(propertiesFile);
        properties.load(inputStream);
        String debug = properties.getProperty("debug", "false");
        String url = properties.getProperty("url", "");
        String backupUrls = properties.getProperty("backupUrls", "");
        String connectTimeout = properties.getProperty("connectTimeout", "0");
        String readTimeout = properties.getProperty("readTimeout", "0");
        String writeTimeout = properties.getProperty("writeTimeout", "0");
        String retryCount = properties.getProperty("retryCount", "0");

        OkConfig.Builder builder = builder();
        builder.debug(Boolean.valueOf(debug));
        builder.url(url, backupUrls.split(","));
        builder.connectTimeout(Integer.valueOf(connectTimeout));
        builder.readTimeout(Integer.valueOf(readTimeout));
        builder.writeTimeout(Integer.valueOf(writeTimeout));
        builder.retryCount(Integer.valueOf(retryCount));

        inputStream.close();
    }
}
