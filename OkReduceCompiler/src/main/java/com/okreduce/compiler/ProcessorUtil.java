package com.okreduce.compiler;

import com.squareup.javapoet.ClassName;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class ProcessorUtil {
    private static Messager messager;
    private static Elements elementsUtils;
    private static Types typeUtil;

    public static void init(Messager messager, Elements elementsUtils, Types typeUtil) {
        ProcessorUtil.messager = messager;
        ProcessorUtil.elementsUtils = elementsUtils;
        ProcessorUtil.typeUtil = typeUtil;
    }

    public static Element asElement(TypeMirror mirror) {
        if (mirror == null) {
            return null;
        }
        return ProcessorUtil.typeUtil.asElement(mirror);
    }

    public static boolean isSupportBasicType(String typeName) {
        return isJavaObjectBasicType(typeName) || isJavaBasicType(typeName);
    }

    public static boolean isJavaBasicType(String typeName) {
        return ("int".equals(typeName) || "long".equals(typeName) || "double".equals(typeName) || "boolean".equals(typeName)
                || "short".equals(typeName) || "float".equals(typeName));
    }

    public static boolean isJavaObjectBasicType(String typeName) {
        return ("java.lang.Int".equals(typeName) || "java.lang.Long".equals(typeName) || "java.lang.Double".equals(typeName)
                || "java.lang.Boolean".equals(typeName) || "java.lang.Short".equals(typeName) || "java.lang.Float".equals(typeName)
                || "java.lang.String".equals(typeName));
    }

    public static boolean isDirectSupportType(TypeMirror mirror) {
        String type = mirror.toString();
        if (type.indexOf("java.io.File") != -1
                || type.indexOf("org.json.JSONObject") != -1
                || type.indexOf("java.lang.String") != -1) {
            return true;
        }
        return false;
    }

    public static boolean isCollectionSupportType(TypeMirror mirror) {
        String type = mirror.toString();
        if (type.indexOf("java.util.List") != -1
                || type.indexOf("java.util.ArrayList") != -1
                || type.indexOf("java.util.Set") != -1
                || type.indexOf("java.util.HashSet") != -1
                || type.indexOf("java.util.Map") != -1
                || type.indexOf("java.util.Collection") != -1) {
            return true;
        }
        return false;
    }

    public static boolean isMapSupportType(TypeMirror mirror) {
        String type = mirror.toString();
        if (type.indexOf("java.util.Map") != -1) {
            return true;
        }
        return false;
    }

    public static boolean isCollectionSupportGenericsType(TypeMirror mirror) {
        TypeElement element = getCollectionGenericsType(mirror);
        if (element == null) {
            return false;
        }
        return !isNotSupportParserType(element.asType());
    }


    public static boolean isNotSupportParserType(TypeMirror mirror) {
        String typeName = mirror.toString();
        if (isJavaBasicType(typeName)) {
            return true;
        }
        if (typeName.startsWith("void")) {
            return true;
        }
        if (typeName.startsWith("java.")) {
            return true;
        }
        return false;
    }

    public static boolean isVoidElement(TypeMirror mirror) {
        if (mirror == null) {
            return false;
        }
        if ("void".equals(mirror.toString())) {
            return true;
        }
        return false;
    }

    public static boolean isFileType(TypeMirror mirror) {
        String type = mirror.toString();
        if (type.indexOf("java.io.File") != -1) {
            return true;
        }
        return false;
    }

    public static boolean isCollectionElement(Element element) {
        if (element == null) {
            return false;
        }

        String type = element.asType().toString();
        if (type.indexOf("java.util.List") != -1
                || type.indexOf("java.util.ArrayList") != -1
                || type.indexOf("java.util.Set") != -1
                || type.indexOf("java.util.HashSet") != -1
                || type.indexOf("java.util.Collection") != -1) {
            return true;
        }
        return false;
    }

    public static boolean isGenericsType(TypeMirror mirror) {
        String type = mirror.toString();
        if (type.indexOf("<") < 0) {
            return false;
        }
        return true;
    }

    public static TypeElement getCollectionGenericsType(TypeMirror mirror) {
        String type = mirror.toString();
        if (type.indexOf("<") < 0) {
            return null;
        }
        String typeName = type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
        TypeElement element = ProcessorUtil.elementsUtils.getTypeElement(typeName);
        if (element == null) {
            int firstIndex = typeName.indexOf("<");
            if (firstIndex >= 0) {
                typeName = typeName.substring(0, firstIndex);
                element = ProcessorUtil.elementsUtils.getTypeElement(typeName);
            }
        }
        return element;
    }

    public static TypeElement getCollectionGenericsType(Element element) {
        String typeName = getCollectionGenericsTypeName(element);
        return ProcessorUtil.elementsUtils.getTypeElement(typeName);
    }

    public static String getCollectionGenericsTypeName(Element element) {
        String type = element.asType().toString();
        if (type.indexOf("<") < 0) {
            return null;
        }
        String typeName = type.substring(type.indexOf("<") + 1, type.indexOf(">"));
        return typeName;
    }

    public static Object getAnnotationValue(AnnotationMirror mirror, String method) {
        if (mirror == null || method == null || method.isEmpty()) {
            return null;
        }
        Map<ExecutableElement, AnnotationValue> valueMap = (Map<ExecutableElement, AnnotationValue>) mirror.getElementValues();
        Iterator<Map.Entry<ExecutableElement, AnnotationValue>> it = valueMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ExecutableElement, AnnotationValue> entry = it.next();
            ExecutableElement executable = entry.getKey();
            AnnotationValue annotationValue = entry.getValue();
            if (executable.getSimpleName().toString().equals(method)) {
                return annotationValue.getValue();
            }
        }
        return null;
    }

    public static ClassName createClientClass() {
        return ClassName.get("okhttp3", "OkHttpClient");
    }

    public static void writeMessage(Diagnostic.Kind kind, String msg, Element element) {
        ProcessorUtil.messager.printMessage(kind, msg, element);
    }

    public static void writeMessage(Diagnostic.Kind kind, String msg) {
        ProcessorUtil.messager.printMessage(kind, msg);
    }

    public static AnnotationMirror getFiledAnnotationMirror(String annoName, VariableElement element) {
        List<AnnotationMirror> list = (List<AnnotationMirror>) element.getAnnotationMirrors();
        if (list == null || list.isEmpty()) {
            return null;
        }
        for (AnnotationMirror mirror : list) {
            if (mirror.toString().indexOf(annoName)>=0) {
                return mirror;
            }
        }
        return null;
    }
}
