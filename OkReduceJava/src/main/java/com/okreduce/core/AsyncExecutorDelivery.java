package com.okreduce.core;

public class AsyncExecutorDelivery extends ExecutorDelivery {
    @Override
    public void execute(Runnable runnable) {
        runnable.run();
    }
}
