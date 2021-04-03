package com.okreduce.util;

import java.util.Iterator;
import java.util.Map;

public class MapUtil {
    public static void map(Map map, MapPut mapPut) {
        if (map == null) {
            return;
        }

        Iterator<Map.Entry> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = it.next();
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                value = "";
            }
            mapPut.map(key, value);
        }
    }

    public interface MapPut {
        void map(String key, Object value);
    }
}
