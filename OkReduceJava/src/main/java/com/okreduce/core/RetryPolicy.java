package com.okreduce.core;

public class RetryPolicy {
    private int retryCount;

    private int currentRetryCount;

    public RetryPolicy(int retryCount){
        this.retryCount=retryCount;
    }

    public boolean retry() {
        currentRetryCount++;
        if (currentRetryCount > retryCount) {
            return false;
        }
        return true;
    }

    public int getCurrentRetryCount() {
        return currentRetryCount;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
