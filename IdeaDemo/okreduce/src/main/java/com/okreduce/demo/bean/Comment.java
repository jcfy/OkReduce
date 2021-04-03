package com.okreduce.demo.bean;

public class Comment {
    public String content;
    public long timestamp;

    @Override
    public String toString() {
        return "Comment{" +
                "content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
