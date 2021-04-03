package com.okreduce.core;

public interface OkCallback<T> {
    /**
     * 成功响应数据，UI线程执行
     */
    void onSucceed(T entity);

    /**
     * 失败请求，UI线程执行
     */
    void onError(int code,String errorBody,Exception e);
}
