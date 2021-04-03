package com.okreduce.core;

import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;

public interface IParser {
    String toJson(OkRequest request,Object param) throws Exception;
    Object toEntity(OkRequest request,InputStream input) throws Exception;
}
