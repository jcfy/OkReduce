package com.okreduce.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class GsonParserTypeElement extends ParserTypeElement {
    public GsonParserTypeElement(ClassName apiClass, TypeMirror typeMirror) {
        super(apiClass, typeMirror);
    }

    @Override
    protected void toJsonMethodCode(MethodSpec.Builder builder) {
        ClassName gsonType = ClassName.get("com.google.gson", "Gson");
        builder.addStatement("return new $T().toJson(entity)", gsonType);
    }

    @Override
    protected void toEntityMethodCode(MethodSpec.Builder builder) {
        ClassName gsonType = ClassName.get("com.google.gson", "Gson");
        ClassName gsonBuilderType = ClassName.get("com.google.gson", "GsonBuilder");
        builder.addStatement("$T gson = new $T().create()", gsonType, gsonBuilderType);

        TypeMirror mirror = getTypeMirror();
        ClassName streamUtilClass = ClassName.get("com.okreduce.util", "StreamUtil");
        if (ProcessorUtil.isGenericsType(mirror)) {
            ClassName tokenType = ClassName.get("com.google.gson.reflect", "TypeToken");
            builder.addStatement("return gson.fromJson($T.readString(input), new $T<$N>() {}.getType())", streamUtilClass,tokenType,mirror.toString());
        } else {
            builder.addStatement("return gson.fromJson($T.readString(input), $T.class)",streamUtilClass,mirror);
        }
    }
}
