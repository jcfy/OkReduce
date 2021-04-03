package com.okreduce.core;

import java.util.concurrent.Executor;

public abstract class ExecutorDelivery implements Executor {

    public void sendResponse(OkCallback callBack, Object entity) {
        if (callBack == null) {
            return;
        }
        PostResponse postResponse = new PostResponse(callBack, entity);
        execute(postResponse);
    }

    public void sendError(int code, String body, OkCallback callBack, Exception exception) {
        if (callBack == null) {
            return;
        }
        PostError postError = new PostError(code, body, callBack, exception);
        execute(postError);
    }

    private class PostResponse implements Runnable {
        private OkCallback callBack;
        private Object entity;

        public PostResponse(OkCallback callBack, Object entity) {
            this.callBack = callBack;
            this.entity = entity;
        }

        @Override
        public void run() {
            callBack.onSucceed(entity);
        }
    }

    private class PostError implements Runnable {
        private OkCallback callBack;
        private Exception exception;
        private int code;
        private String body;

        public PostError(int code, String body, OkCallback callBack, Exception exception) {
            this.code = code;
            this.body = body;
            this.callBack = callBack;
            this.exception = exception;
        }

        @Override
        public void run() {
            callBack.onError(code, body, exception);
        }
    }
}
