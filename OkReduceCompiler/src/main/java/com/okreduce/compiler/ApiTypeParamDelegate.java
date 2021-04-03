package com.okreduce.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

public class ApiTypeParamDelegate {
    private ClassName apiClass;
    private List<? extends VariableElement> paramElementList;

    public ApiTypeParamDelegate(ClassName apiClass, List<? extends VariableElement> paramElementList) {
        this.apiClass = apiClass;
        this.paramElementList = paramElementList;
    }

    public void putAll(TypeSpec.Builder builder) {
        putInitialValueMethod(builder);
        if (isExistEntityAnnotation()) {
            putEntityMethodSpecList(builder);
            putFillMethodSpec(builder);
        } else {
            putParamMethodSpecList(builder);
            putFillMethodSpec(builder);
        }
    }

    private void putInitialValueMethod(TypeSpec.Builder typeBuilder) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("initialValue")
                .addModifiers(Modifier.PRIVATE);
        if (paramElementList == null || paramElementList.isEmpty()) {
            typeBuilder.addMethod(builder.build());
            return;
        }
        for (VariableElement element : paramElementList) {
            String paramName = element.getSimpleName().toString();
            AnnotationMirror headMirror = ProcessorUtil.getFiledAnnotationMirror("com.okreduce.annotation.OkHead", element);
            AnnotationMirror paramMirror = ProcessorUtil.getFiledAnnotationMirror("com.okreduce.annotation.OkParam", element);
            if (headMirror != null) {
                String initial = (String) ProcessorUtil.getAnnotationValue(headMirror, "initial");
                if (initial != null && !initial.isEmpty()) {
                    String value = (String) ProcessorUtil.getAnnotationValue(headMirror, "value");
                    paramName = (value == null || value.isEmpty()) ? paramName : value;
                    builder.addStatement("headerMap.put(\"" + paramName + "\", \"" + initial + "\")");
                }
            } else if (paramMirror != null) {
                String initial = (String) ProcessorUtil.getAnnotationValue(paramMirror, "initial");
                if (initial != null && !initial.isEmpty()) {
                    String value = (String) ProcessorUtil.getAnnotationValue(paramMirror, "value");
                    paramName = (value == null || value.isEmpty()) ? paramName : value;
                    builder.addStatement("paramMap.put(\"" + paramName + "\", \"" + initial + "\")");
                }
            }
        }
        typeBuilder.addMethod(builder.build());
    }

    private void putEntityMethodSpecList(TypeSpec.Builder builder) {
        if (paramElementList == null || paramElementList.isEmpty()) {
            return;
        }
        int entityCount = 0;
        for (VariableElement element : paramElementList) {
            AnnotationMirror entityMirror = ProcessorUtil.getFiledAnnotationMirror("com.okreduce.annotation.OkEntity", element);
            AnnotationMirror headMirror = ProcessorUtil.getFiledAnnotationMirror("com.okreduce.annotation.OkHead", element);
            if (entityMirror == null && headMirror == null) {
                ProcessorUtil.writeMessage(Diagnostic.Kind.ERROR, "Entity parameters are not allowed to have other arguments", element);
                break;
            }
            if (entityCount >= 1 && entityMirror != null) {
                ProcessorUtil.writeMessage(Diagnostic.Kind.ERROR, "Entity parameters are not allowed to have other parameters", element);
                break;
            }
            if (entityMirror != null) {
                entityCount++;
                builder.addMethod(createEntityMethodSpec(element, element.getSimpleName().toString()));
            } else if (!element.asType().toString().startsWith("java.lang.String")) {
                ProcessorUtil.writeMessage(Diagnostic.Kind.ERROR, "@OkHead declaration parameter, only support string type", element);
                break;
            } else {
                String paramName = element.getSimpleName().toString();
                MethodSpec methodSpec = createParamMethodSpec(element, paramName);
                if (methodSpec != null) {
                    builder.addMethod(methodSpec);
                }
            }
        }
    }

    private MethodSpec createEntityMethodSpec(VariableElement element, String paramName) {
        MethodSpec.Builder builder = createMethodSpecBuilder(paramName);
        builder.addParameter(ClassName.get(element.asType()), element.getSimpleName().toString());
        builder.addStatement("this.entity=$N", element.getSimpleName().toString());
        builder.addStatement("return this");
        return builder.build();
    }

    private boolean isExistEntityAnnotation() {
        if (paramElementList == null || paramElementList.isEmpty()) {
            return false;
        }
        for (VariableElement element : paramElementList) {
            AnnotationMirror mirror = ProcessorUtil.getFiledAnnotationMirror("com.okreduce.annotation.OkEntity", element);
            if (mirror != null) {
                return true;
            }
        }
        return false;
    }

    private void putFillMethodSpec(TypeSpec.Builder typeBuilder) {
        if (paramElementList == null || paramElementList.isEmpty()) {
            return;
        }
        MethodSpec.Builder builder = createMethodSpecBuilder("fill");
        for (VariableElement element : paramElementList) {
            builder.addParameter(ClassName.get(element.asType()), element.getSimpleName().toString());
        }
        for (VariableElement element : paramElementList) {
            String paramName = element.getSimpleName().toString();
            builder.addStatement("this." + paramName + "(" + paramName + ")");
        }
        builder.addStatement("return this");
        typeBuilder.addMethod(builder.build());
    }

    private void putFiledValueStatement(MethodSpec.Builder builder, VariableElement element) {
        String paramName = element.getSimpleName().toString();
        boolean isBasicsType = element.asType().getKind().isPrimitive();
        if (!isBasicsType) {
            builder.beginControlFlow("if(" + paramName + "!=null)");
        }
        AnnotationMirror headMirror = ProcessorUtil.getFiledAnnotationMirror("com.okreduce.annotation.OkHead", element);
        if (headMirror != null) {
            String value = (String) ProcessorUtil.getAnnotationValue(headMirror, "value");
            paramName = (value == null || value.isEmpty()) ? paramName : value;
            builder.addStatement("headerMap.put(\"" + paramName + "\", " + element.getSimpleName().toString() + ")");
        } else {
            AnnotationMirror paramMirror = ProcessorUtil.getFiledAnnotationMirror("com.okreduce.annotation.OkParam", element);
            String value = (String) ProcessorUtil.getAnnotationValue(paramMirror, "value");
            paramName = (value == null || value.isEmpty()) ? paramName : value;
            builder.addStatement("paramMap.put(\"" + paramName + "\", " + element.getSimpleName().toString() + ")");
        }
        if (!isBasicsType) {
            builder.endControlFlow();
        }
    }

    private void putParamMethodSpecList(TypeSpec.Builder builder) {
        if (paramElementList == null || paramElementList.isEmpty()) {
            return;
        }
        for (VariableElement element : paramElementList) {
            AnnotationMirror headMirror = ProcessorUtil.getFiledAnnotationMirror("com.okreduce.annotation.OkHead", element);
            if (headMirror != null && !element.asType().toString().startsWith("java.lang.String")) {
                ProcessorUtil.writeMessage(Diagnostic.Kind.ERROR, "@OkHead declaration parameter, only support string type", element);
                break;
            }
            String paramName = element.getSimpleName().toString();
            MethodSpec methodSpec = createParamMethodSpec(element, paramName);
            if (methodSpec != null) {
                builder.addMethod(methodSpec);
            }
        }
    }

    private MethodSpec createParamMethodSpec(VariableElement element, String paramName) {
        MethodSpec.Builder builder = createMethodSpecBuilder(paramName);
        builder.addParameter(ClassName.get(element.asType()), element.getSimpleName().toString());
        putFiledValueStatement(builder, element);
        builder.addStatement("return this");
        return builder.build();
    }

    private MethodSpec.Builder createMethodSpecBuilder(String methodName) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(apiClass);
        return builder;
    }

}
