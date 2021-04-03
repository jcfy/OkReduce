package com.okreduce.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public class MainCodeElementManager {
    private String packageName;
    private CollectionElementManager collectionManager;
    private boolean isAndroidPlatform;

    public MainCodeElementManager(String packageName, boolean isAndroidPlatform) {
        this.packageName = packageName;
        collectionManager = new CollectionElementManager();
        this.isAndroidPlatform = isAndroidPlatform;
    }

    public void init(Set<Element> apiTypeElementSet) {
        for (Element element : apiTypeElementSet) {
            if (element instanceof TypeElement) {
                loopDisposeApiMethod((TypeElement) element);
            }
        }
    }

    private void loopDisposeApiMethod(TypeElement typeElement) {
        List<Element> elementList = new ArrayList(typeElement.getEnclosedElements());
        if (elementList == null || elementList.isEmpty()) {
            return;
        }

        for (Element element : elementList) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement methodElement = (ExecutableElement) element;
            TypeMirror mirror = methodElement.getReturnType();
            if (!mirror.toString().startsWith("byte[]") && mirror.getKind().isPrimitive()) {
                ProcessorUtil.writeMessage(Diagnostic.Kind.ERROR, "Unsupported return type: " + mirror, methodElement);
                return;
            }
            String apiClassName = getApiClassNameName(methodElement);
            if (collectionManager.checkExistTypeElement(apiClassName)) {
                String tip = "All interfaces cannot have the same method name. It is recommended to define it as " + methodElement.getSimpleName() + "1 or " + methodElement.getSimpleName() + "2";
                ProcessorUtil.writeMessage(Diagnostic.Kind.ERROR, tip, methodElement);
                return;
            }
            ApiTypeElement apiTypeElement = new ApiTypeElement(packageName, apiClassName, typeElement, methodElement, isAndroidPlatform);
            collectionManager.addTypeElement(apiClassName, apiTypeElement);
        }
    }


    public boolean writeTo(Filer filer, File saveFile) {
        Iterator<Map.Entry<String, Object>> it = collectionManager.getAllWriteTypeElement();
        if (it == null) {
            return false;
        }
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            Object item = entry.getValue();
            TypeSpec typeSpec = null;
            ClassName classType = null;
            if (item instanceof ApiTypeElement) {
                ApiTypeElement typeElement = (ApiTypeElement) item;
                typeSpec = typeElement.createTypeSpec();
                classType = typeElement.getApiClassName();
            }

            if (typeSpec == null || classType == null) {
                continue;
            }

            JavaFile javaFile = JavaFile.builder(classType.packageName(), typeSpec).build();
            try {
                if (saveFile != null) {
                    javaFile.writeTo(saveFile);
                } else {
                    javaFile.writeTo(filer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public String getApiClassNameName(ExecutableElement methodElement) {
        String name = methodElement.getSimpleName().toString();
        String letter = name.substring(0, 1).toUpperCase();
        String apiName = letter + name.substring(1, name.length());
        return apiName;
    }
}
