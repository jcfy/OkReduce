package com.okreduce.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    private Boolean debug;
    private Logger logger = Logger.getLogger("OkHttpReduce");

    public Log(Boolean debug) {
        this.debug = debug;
    }

    public void info(String tag, String content) {
        println(Level.INFO, tag, content);
    }

    public void warn(String tag, String content) {
        println(Level.WARNING, tag, content);
    }

    private void println(Level level, String tag, String content) {
        if (debug == null || !debug) {
            return;
        }
        String newTag = tag;
        if (tag != null && !tag.isEmpty()) {
            newTag = tag + "\n";
        }
        logger.log(level, newTag + content + "\n");
    }
}
