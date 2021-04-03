package com.okreduce.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public abstract class ParserTypeElement {
    private TypeMirror typeMirror;
    private ClassName apiClass;

    public ParserTypeElement(ClassName apiClass, TypeMirror typeMirror) {
        this.typeMirror = typeMirror;
        this.apiClass = apiClass;
    }

    public TypeMirror getTypeMirror() {
        return typeMirror;
    }

    public TypeSpec createTypeSpec() {
        TypeSpec.Builder builder = TypeSpec.classBuilder("OkApiParser").addModifiers(Modifier.PUBLIC,Modifier.STATIC);
        ClassName absParser = ClassName.get("com.okreduce.core", "AbstractParser");
        builder.superclass(absParser);
        MethodSpec toJsonMethodSpec = createToJsonMethod();
        MethodSpec toEntityMethodSpec = createToEntityMethod();
        builder.addMethod(toJsonMethodSpec);
        builder.addMethod(toEntityMethodSpec);
        return builder.build();
    }

    private MethodSpec createToJsonMethod() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toJson").addModifiers(Modifier.PUBLIC);
        methodBuilder.addAnnotation(Override.class);
        methodBuilder.addParameter(ClassName.get("com.okreduce.core", "OkRequest"), "request");
        methodBuilder.addParameter(Object.class, "entity");
        methodBuilder.returns(String.class);
        methodBuilder.addException(Exception.class);
        toJsonMethodCode(methodBuilder);
        return methodBuilder.build();
    }

    private MethodSpec createToEntityMethod() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("toEntity").addModifiers(Modifier.PUBLIC);
        methodBuilder.addAnnotation(Override.class);
        methodBuilder.addParameter(ClassName.get("com.okreduce.core", "OkRequest"), "request");
        methodBuilder.addParameter(ClassName.get(InputStream.class), "input");
        methodBuilder.returns(Object.class);
        methodBuilder.addException(Exception.class);
        if (!toDirectSupportMethodCode(methodBuilder)) {
            toEntityMethodCode(methodBuilder);
        }
        return methodBuilder.build();
    }

    private boolean toDirectSupportMethodCode(MethodSpec.Builder builder) {
        ClassName streamUtilClass = ClassName.get("com.okreduce.util", "StreamUtil");
        String type = typeMirror.toString();
        if (type.startsWith("void") || type.startsWith("java.lang.Void")) {
            builder.addStatement("return null");
            return true;
        } else if (type.startsWith("java.lang.String")) {
            builder.addStatement("return $T.readString(input)", streamUtilClass);
            return true;
        } else if (type.startsWith("byte")) {
            builder.addStatement(" return  $T.readStream(input)", streamUtilClass);
            return true;
        } else if (type.startsWith("java.io.InputStream")) {
            builder.addStatement("return input");
            return true;
        } else if (type.startsWith("java.io.File")) {
            builder.addStatement("$T saveFile", File.class);
            ClassName utilClass = ClassName.get("com.okreduce.util", "Util");

            builder.beginControlFlow("if(request.getConfig().getSavePath() != null)");
            builder.addStatement("saveFile = new File(request.getConfig().getSavePath(), $T.getUUID())", utilClass);
            builder.endControlFlow();
            builder.beginControlFlow("else");
            builder.addStatement("saveFile = File.createTempFile($T.getUUID(), \"\")", utilClass);
            builder.endControlFlow();

            builder.beginControlFlow("if(!saveFile.getParentFile().exists())");
            builder.addStatement("saveFile.getParentFile().mkdirs()");
            builder.endControlFlow();

            builder.addStatement("$T outputStream = new FileOutputStream(saveFile)", FileOutputStream.class);
            builder.addStatement("$T.outputStream(input, outputStream)", streamUtilClass);
            builder.addStatement(" return saveFile", streamUtilClass);
            return true;
        }
        return false;
    }

    protected abstract void toJsonMethodCode(MethodSpec.Builder builder);

    protected abstract void toEntityMethodCode(MethodSpec.Builder builder);
}
