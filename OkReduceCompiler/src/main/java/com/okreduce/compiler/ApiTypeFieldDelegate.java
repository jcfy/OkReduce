package com.okreduce.compiler;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class ApiTypeFieldDelegate {
    private Map<String, AnnotationMirror> apiAnnoMap;
    private Map<String, AnnotationMirror> methodAnnoMap;

    public ApiTypeFieldDelegate(TypeElement typeElement, ExecutableElement methodElement) {
        apiAnnoMap = getAnnotationMap(typeElement);
        methodAnnoMap = getAnnotationMap(methodElement);
    }

    public void putAll(TypeSpec.Builder builder) {
        putDynaFieldType(builder);
    }

    private void putDynaFieldType(TypeSpec.Builder builder) {
        builder.addField(createConstantSpec(ClassName.get(String.class), "URL", getValue("url", null), true));
        Object backupUrls = getValue("urls", null);
        if (backupUrls != null) {
            backupUrls = "{" + backupUrls + "}";
        }
        builder.addField(createConstantSpec(ArrayTypeName.get(String[].class), "BACKUP_URLS",backupUrls, false));
        builder.addField(createConstantSpec(ClassName.get(String.class), "URI",
                getValue("uri", ""), true));

        builder.addField(createConstantSpec(ClassName.get(String.class), "LOG", getConfigValue("log", ""), true));
        ClassName methodClass = ClassName.get("com.okreduce.constant", "Method");
        builder.addField(createConstantSpec(methodClass, "METHOD", getMethodValue(), false));

        ClassName contentTypeClass = ClassName.get("com.okreduce.constant", "ContentType");
        Object type = getValue("type", null);
        if (type != null) {
            builder.addField(createConstantSpec(contentTypeClass, "CONTENT_TYPE", "ContentType." + type, false));
        } else {
            builder.addField(createConstantSpec(contentTypeClass, "CONTENT_TYPE", null, false));
        }

        ClassName debugClass = ClassName.get(Boolean.class);
        Object debug = getConfigValue("debug", null);
        builder.addField(createConstantSpec(debugClass, "DEBUG", debug, false));

        builder.addField(createConstantSpec(ClassName.INT, "CONNECT_TIMEOUT", getConfigValue("connectout", 0), false));
        builder.addField(createConstantSpec(ClassName.INT, "READ_TIMEOUT", getConfigValue("readout", 0), false));
        builder.addField(createConstantSpec(ClassName.INT, "WRITE_TIMEOUT", getConfigValue("writeout", 0), false));
        builder.addField(createConstantSpec(ClassName.INT, "RETRY_COUNT", getConfigValue("retry", 0), false));

        builder.addField(createFieldSpec(ClassName.get("com.okreduce.core", "OkConfig"),
                "config", null));
        builder.addField(createFieldSpec(ClassName.get(String.class), "uri", "URI"));
        ClassName deliveryClass = ClassName.get("com.okreduce.core", "ExecutorDelivery");
        builder.addField(createFieldSpec(deliveryClass, "delivery", null));
        builder.addField(createFieldSpec(ClassName.get(Object.class), "entity", null));

        builder.addField(createMapFieldSpec("headerMap", String.class));
        builder.addField(createMapFieldSpec("paramMap", Object.class));
    }

    private String getMethodValue() {
        AnnotationMirror mirror = methodAnnoMap.get("OkPost");
        if (mirror != null) {
            return "Method.POST";
        }
        mirror = methodAnnoMap.get("OkGet");
        if (mirror != null) {
            return "Method.GET";
        }
        mirror = apiAnnoMap.get("OkPost");
        if (mirror != null) {
            return "Method.POST";
        }
        mirror = apiAnnoMap.get("OkGet");
        if (mirror != null) {
            return "Method.GET";
        }
        return null;
    }

    private FieldSpec createConstantSpec(TypeName clazz, String fieldName, Object value, boolean isString) {
        FieldSpec.Builder builder = FieldSpec.builder(clazz, fieldName, Modifier.PRIVATE,
                Modifier.STATIC, Modifier.FINAL);
        if (isString && value != null) {
            builder.initializer("\"" + value + "\"");
        } else{
            builder.initializer(String.valueOf(value));
        }
        return builder.build();
    }

    private FieldSpec createFieldSpec(TypeName clazz, String fieldName, String valueName) {
        FieldSpec.Builder builder = FieldSpec.builder(clazz, fieldName, Modifier.PRIVATE);
        if (valueName != null) {
            builder.initializer(valueName);
        }
        return builder.build();
    }

    private FieldSpec createMapFieldSpec(String fieldName, Class className) {
        TypeName type = ParameterizedTypeName.get(ClassName.get(Map.class)
                , ClassName.get(String.class), TypeName.get(className));
        FieldSpec.Builder builder = FieldSpec.builder(type, fieldName, Modifier.PRIVATE);
        builder.initializer("new $T<>()", HashMap.class);
        return builder.build();
    }

    private Map<String, AnnotationMirror> getAnnotationMap(Element element) {
        List<AnnotationMirror> list = (List<AnnotationMirror>) element.getAnnotationMirrors();
        if (list == null || list.isEmpty()) {
            return new HashMap();
        }
        Map<String, AnnotationMirror> mirrorMap = new HashMap();
        for (AnnotationMirror mirror : list) {
            String name = mirror.getAnnotationType().asElement().getSimpleName().toString();
            mirrorMap.put(name, mirror);
        }
        return mirrorMap;
    }

    private Object getValue(String method, String defaultValue) {
        Object value = getAnnotationValue("OkPost", method, methodAnnoMap);
        if (value == null) {
            value = getAnnotationValue("OkGet", method, methodAnnoMap);
        }
        if (value == null) {
            value = getAnnotationValue("OkPost", method, apiAnnoMap);
        }
        if (value == null) {
            value = getAnnotationValue("OkGet", method, apiAnnoMap);
        }
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    private Object getConfigValue(String method, Object defaultValue) {
        Object value = getAnnotationValue("OkConfig", method, methodAnnoMap);
        if (value == null) {
            value = getAnnotationValue("OkConfig", method, apiAnnoMap);
        }
        if (value == null) {
            return defaultValue;
        }
        return value;
    }


    private Object getAnnotationValue(String clazz, String method, Map<String, AnnotationMirror> map) {
        AnnotationMirror mirror = map.get(clazz);
        if (mirror == null) {
            return null;
        }
        return ProcessorUtil.getAnnotationValue(mirror, method);
    }
}
