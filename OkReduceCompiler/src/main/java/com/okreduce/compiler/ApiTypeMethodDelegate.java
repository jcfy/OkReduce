package com.okreduce.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

public class ApiTypeMethodDelegate {
    private ClassName apiClass;
    private ExecutableElement methodElement;
    private boolean isAndroidPlatform;

    public ApiTypeMethodDelegate(ClassName apiClass, ExecutableElement methodElement, boolean isAndroidPlatform) {
        this.apiClass = apiClass;
        this.methodElement = methodElement;
        this.isAndroidPlatform = isAndroidPlatform;
    }

    public void putAll(TypeSpec.Builder builder) {
        builder.addMethod(createMethodForApi("uri", ClassName.get(String.class), "uri"));

        builder.addMethod(createMethodForMap("header", "headerMap",
                ClassName.get(String.class),"key",ClassName.get(String.class),"value"));
        builder.addMethod(createMethodForMap("param", "paramMap",
                ClassName.get(String.class),"key",ClassName.get(Object.class),"value"));

        if (isAndroidPlatform) {
            builder.addMethod(createInThreadMethod());
        }
        builder.addMethod(createOrigExecuteMethod());
        builder.addMethod(createOrigEnqueueMethod());
        builder.addMethod(createExecuteMethod());
        builder.addMethod(createEnqueueMethod());
    }

    private MethodSpec createOrigExecuteMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("origExecute")
                .addModifiers(Modifier.PUBLIC)
                .addException(ClassName.get(Exception.class))
                .returns(ClassName.get("okhttp3", "Response"));
        ClassName requestClass= ClassName.get("com.okreduce.core", "OkRequest");
        ClassName executorClass= ClassName.get("com.okreduce", "OKHttpExecutor");
        builder.addStatement("$T request=new OkRequest(config, uri, headerMap, paramMap, entity)",requestClass);
        builder.addStatement("$T executor = new OKHttpExecutor(request)",executorClass);
        builder.addStatement("return executor.origExecute()");
        return builder.build();
    }

    private MethodSpec createOrigEnqueueMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("origEnqueue")
                .addParameter(ClassName.get("okhttp3", "Callback"), "callBack")
                .addModifiers(Modifier.PUBLIC);
        builder.addStatement("OkRequest request=new OkRequest(config, uri, headerMap, paramMap, entity)");
        builder.addStatement("OKHttpExecutor executor = new OKHttpExecutor(request)");
        builder.addStatement("executor.origEnqueue(callBack)");
        return builder.build();
    }

    private MethodSpec createExecuteMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("execute")
                .addException(ClassName.get(Exception.class))
                .addModifiers(Modifier.PUBLIC);
        String typeString = methodElement.getReturnType().toString();
        if (!(typeString.startsWith("void") && !typeString.startsWith("java.lang.Void"))) {
            builder.returns(TypeName.get(methodElement.getReturnType()));
        }
        builder.addStatement("OkRequest request=new OkRequest(config, uri, headerMap, paramMap, entity)");
        builder.addStatement("OKHttpExecutor executor = new OKHttpExecutor(request)");

        if (!(typeString.startsWith("void") && !typeString.startsWith("java.lang.Void"))) {
            builder.addStatement("return ($T) executor.execute()", methodElement.getReturnType());
        }else{
            builder.addStatement("executor.execute()");
        }
        return builder.build();
    }

    private MethodSpec createEnqueueMethod() {
        ClassName apiCallbackClass = ClassName.get("com.okreduce.core", "OkCallback");
        MethodSpec.Builder builder = MethodSpec.methodBuilder("enqueue")
                .addModifiers(Modifier.PUBLIC);
        String typeString = methodElement.getReturnType().toString();
        if (!(typeString.startsWith("void") && !typeString.startsWith("java.lang.Void"))) {
            ParameterizedTypeName mapType = ParameterizedTypeName.get(apiCallbackClass, TypeName.get(methodElement.getReturnType()));
            builder.addParameter(mapType, "callBack");
        } else {
            builder.addParameter(apiCallbackClass, "callBack");
        }
        builder.addStatement("OkRequest request=new OkRequest(config, uri, headerMap, paramMap, entity)");
        TypeName type;
        if (isAndroidPlatform) {
            type = ClassName.get("com.okreduce.core", "AndroidExecutorDelivery");
        } else {
            type = ClassName.get("com.okreduce.core", "AsyncExecutorDelivery");
        }
        builder.addStatement("ExecutorDelivery delivery = this.delivery != null ? this.delivery : new $T()", type);
        builder.addStatement("OKHttpExecutor executor = new OKHttpExecutor(request, delivery)");
        builder.addStatement("executor.enqueue(callBack)");
        return builder.build();
    }

    private MethodSpec createInThreadMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("inThread")
                .addModifiers(Modifier.PUBLIC)
                .returns(apiClass);
        builder.addStatement("delivery = new $T()", ClassName.get("com.okreduce.core", "AsyncExecutorDelivery"));
        builder.addStatement("return this");
        return builder.build();
    }

//    private MethodSpec createMethodForParamMap(String methodName, String mapName, Class clazz) {
//        MethodSpec.Builder builder = createMethodBuild(methodName);
//        ParameterizedTypeName mapType = ParameterizedTypeName.get(Map.class, String.class, clazz);
//        builder.addParameter(mapType, "map");
//        builder.beginControlFlow("if (map != null)");
//        builder.addStatement("$N= map", mapName);
//        builder.endControlFlow();
//        builder.addStatement("return this", apiClass);
//        return builder.build();
//    }

    private MethodSpec createMethodForMap(String methodName, String mapName, TypeName paramType1, String paramName1, TypeName paramType2, String paramName2) {
        MethodSpec.Builder builder = createMethodBuild(methodName);
        builder.addParameter(paramType1, paramName1);
        builder.addParameter(paramType2, paramName2);
        builder.addStatement("$N.put(key, value)", mapName);
        builder.addStatement("return this", apiClass);
        return builder.build();
    }

    private MethodSpec createMethodForApi(String methodName, TypeName paramType, String paramName) {
        MethodSpec.Builder builder = createMethodBuild(methodName);
        builder.addParameter(paramType, paramName);
        builder.addStatement("this.$N = $N", paramName, paramName);
        builder.addStatement("return this", apiClass);
        return builder.build();
    }

    private MethodSpec.Builder createMethodBuild(String methodName) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(apiClass);
        return builder;
    }
}
