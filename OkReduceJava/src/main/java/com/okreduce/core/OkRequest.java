package com.okreduce.core;

import java.util.HashMap;
import java.util.Map;

public class OkRequest {
    private OkConfig config;
    private String uri;
    private Map<String, String> headerMap;
    private Map<String, Object> paramMap;
    private Object entity;

    public OkRequest(OkConfig config, String uri, Map<String, String> headerMap, Map<String, Object> paramMap, Object entity) {
        this.config = config;
        this.entity = entity;
        this.uri = uri;

        if (headerMap == null) {
            this.headerMap = new HashMap<>();
        } else {
            this.headerMap = headerMap;
        }
        if (paramMap == null) {
            this.paramMap = new HashMap<>();
        } else {
            this.paramMap = paramMap;
        }
    }

    public OkConfig getConfig() {
        return config;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public Map<String, String> getFillHeaderMap() {
        Map<String, String> map = new HashMap<>();
        map.putAll(headerMap);
        map.putAll(config.getHeaderMap());
        return map;
    }

    public Map<String, Object> getFillParamMap() {
        Map<String, Object> map = new HashMap<>();
        map.putAll(paramMap);
        map.putAll(config.getParamMap());
        return map;
    }

    public Object getEntity() {
        return entity;
    }
}
