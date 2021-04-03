package com.okreduce.core;

import android.os.Handler;
import android.os.Looper;

public class AndroidExecutorDelivery extends ExecutorDelivery {
    private Handler handler=new Handler(Looper.getMainLooper());
    @Override
    public void execute(Runnable runnable) {
        handler.post(runnable);
    }
}
