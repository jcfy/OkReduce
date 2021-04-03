package com.okreduce.core;

import java.util.Iterator;
import java.util.Map;

public class JSONGenerator {
    public String generate(Map<String,Object> map){
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            sb.append("\"" + key + "\":");
            Object value = entry.getValue();
            if (value.getClass().isArray()) {
                Object[] array = (Object[]) value;
                sb.append(genArrayValueForJson(array));
            } else {
                sb.append(genValueForJson(value));
            }
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }

    private String genArrayValueForJson(Object[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for(Object value:array){
            sb.append(genValueForJson(value));
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String genValueForJson(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else {
            return String.valueOf(value);
        }
    }
}
