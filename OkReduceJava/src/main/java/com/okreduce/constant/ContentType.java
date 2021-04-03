package com.okreduce.constant;

public enum  ContentType {
    FORM("application/x-www-form-urlencoded"), JSON("application/json;charset=utf-8");

    private String value;
    ContentType(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }
}
