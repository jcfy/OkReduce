package com.okreduce.compiler;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class ApiTypeElement {
    private ClassName apiClass;
    private ExecutableElement methodElement;
    private ApiTypeFieldDelegate fieldDelegate;
    private ApiTypeParamDelegate paramDelegate;
    private ApiTypeMethodDelegate methodDelegate;

    public ApiTypeElement(String packageName,String apiClassName, TypeElement typeElement, ExecutableElement methodElement,boolean isAndroidPlatform) {
        this.methodElement = methodElement;
        apiClass=ClassName.get(packageName,apiClassName);
        paramDelegate=new ApiTypeParamDelegate(apiClass,methodElement.getParameters());
        fieldDelegate =new ApiTypeFieldDelegate(typeElement,methodElement);
        methodDelegate =new ApiTypeMethodDelegate(apiClass,methodElement,isAndroidPlatform);
    }

    public TypeSpec createTypeSpec() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(apiClass.simpleName())
                .addModifiers(Modifier.PUBLIC);
        fieldDelegate.putAll(builder);
        putConstructorMethod(builder);
        putConfigMethod(builder);

        putApiMethod(builder);
        putApiMethodForClient(builder);

        paramDelegate.putAll(builder);
        methodDelegate.putAll(builder);

        putApiConfigInsideType(builder);
        putParserInsideType(builder);

        return builder.build();
    }

    public void putApiConfigInsideType(TypeSpec.Builder builder){
        TypeSpec apiConfigInsideTypeSpec =new ApiConfigTypeElement(apiClass).createTypeSpec();
        builder.addType(apiConfigInsideTypeSpec);
    }
    public void putParserInsideType(TypeSpec.Builder builder){
        TypeMirror returnType=methodElement.getReturnType();
        TypeSpec parserTypeSpec = new GsonParserTypeElement(apiClass,returnType).createTypeSpec();
        builder.addType(parserTypeSpec);
    }

    private void putConstructorMethod(TypeSpec.Builder typeBuilder) {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder();
        builder.addParameter(ClassName.get("com.okreduce.core","OkConfig"),"config");
        builder.addStatement("this.config = config");
        builder.addStatement("initialValue()");
        builder.addModifiers(Modifier.PUBLIC);
        typeBuilder.addMethod(builder.build());
    }

    private void putApiMethod(TypeSpec.Builder typeBuilder) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("api")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(apiClass);
        builder.addStatement("return new OkApiConfigBuilder().api()", apiClass);
        typeBuilder.addMethod(builder.build());
    }

    private void putApiMethodForClient(TypeSpec.Builder typeBuilder) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("Api")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .addParameter(ClassName.get("com.okreduce.core","OkConfig"),"config")
                .returns(apiClass);
        builder.addStatement("return new $T(config)", apiClass);
        typeBuilder.addMethod(builder.build());
    }

    private void putConfigMethod(TypeSpec.Builder typeBuilder) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("config")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .returns(ClassName.get("", "OkApiConfigBuilder"));
        builder.addStatement("return new OkApiConfigBuilder()");
        typeBuilder.addMethod(builder.build());
    }

    public ClassName getApiClassName() {
        return apiClass;
    }
}
