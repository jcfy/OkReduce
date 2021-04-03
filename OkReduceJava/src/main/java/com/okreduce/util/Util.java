package com.okreduce.util;

import java.util.UUID;

public class Util {
    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
