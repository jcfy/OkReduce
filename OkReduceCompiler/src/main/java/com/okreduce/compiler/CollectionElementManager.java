package com.okreduce.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CollectionElementManager {
    private Map<String, Object> elementMap = new HashMap();

    public boolean checkExistTypeElement(String name){
        return elementMap.containsKey(name);
    }

    public void addTypeElement(String name, ApiTypeElement typeElement) {
        if (!elementMap.containsKey(name)) {
            elementMap.put(name, typeElement);
        }
    }

    public Iterator<Map.Entry<String, Object>> getAllWriteTypeElement() {
        return elementMap.entrySet().iterator();
    }

}
