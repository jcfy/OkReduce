package com.okreduce.core;

import java.io.IOException;

import okhttp3.Response;

public class ResponseException extends IOException {
    private int code;
    private Response response;

    public ResponseException(int code) {
    }

    public ResponseException(int code, String message) {
        super(message);
    }

    public ResponseException(int code, String message, Response response) {
        super(message);
    }

    public int getCode() {
        return code;
    }

    public Response getResponse() {
        return response;
    }
}
