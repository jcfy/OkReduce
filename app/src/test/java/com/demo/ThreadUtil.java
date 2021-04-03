package com.demo;

public class ThreadUtil {
    public static void sleepTestThread() {
        synchronized (ThreadUtil.class) {
            try {
                Thread.sleep(5 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
