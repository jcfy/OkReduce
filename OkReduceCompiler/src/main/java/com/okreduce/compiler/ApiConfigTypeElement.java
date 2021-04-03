package com.okreduce.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.lang.model.element.Modifier;

public class ApiConfigTypeElement {
    private ClassName apiClass;
    public ApiConfigTypeElement(ClassName apiClass){
        this.apiClass=apiClass;
    }
    public TypeSpec createTypeSpec() {
        ClassName builderType = ClassName.get("", "OkApiConfigBuilder");
        ClassName proxyType = ClassName.get("com.okreduce.core", "ApiConfigBuilder");

        TypeSpec.Builder builderTypeBuilder = TypeSpec.classBuilder("OkApiConfigBuilder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .superclass(ParameterizedTypeName.get(proxyType, builderType));

        builderTypeBuilder.addMethod(createConstructorBuilder());
        builderTypeBuilder.addMethod(createBuildMethod());

        return builderTypeBuilder.build();
    }

    private MethodSpec createConstructorBuilder(){
        MethodSpec.Builder methodBuilder= MethodSpec.constructorBuilder();
        methodBuilder.addStatement("super(URL, BACKUP_URLS, METHOD, CONTENT_TYPE, CONNECT_TIMEOUT, READ_TIMEOUT,\n" +
                "WRITE_TIMEOUT, RETRY_COUNT, new OkApiParser(),DEBUG, LOG)");
        return methodBuilder.build();
    }

    private MethodSpec createBuildMethod(){
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("api")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(apiClass);
        methodBuilder.addStatement("return new $T(build())",apiClass);
        return methodBuilder.build();
    }
}
