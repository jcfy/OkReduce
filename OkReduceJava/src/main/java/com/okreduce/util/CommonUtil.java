package com.okreduce.util;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public final class CommonUtil {
    public static boolean isMultipartParam(Map<String,Object> map) {
        if (map == null || map.isEmpty()) {
            return false;
        }
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            if (entry.getValue() instanceof File) {
                return true;
            }else if (entry.getValue() instanceof byte[]) {
                return true;
            }
        }
        return false;
    }

    public static String getGetRequestParam(Map<String,Object> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            String value = String.valueOf(entry.getValue());
            sb.append(key + "=" + value);
            sb.append("&");
        }
        String params = sb.toString();
        if (params.endsWith("&")) {
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
