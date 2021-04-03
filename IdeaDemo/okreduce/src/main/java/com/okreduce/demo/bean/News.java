package com.okreduce.demo.bean;

import java.util.List;

public class News {
    public String title;
    public String content;
    public int readCount;
    public float score;
    public List<Comment> commentList;

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", readCount=" + readCount +
                ", score=" + score +
                ", commentList=" + commentList +
                '}';
    }
}
