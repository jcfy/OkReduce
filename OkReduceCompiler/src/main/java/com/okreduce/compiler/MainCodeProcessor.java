package com.okreduce.compiler;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.DocumentationTool;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.StandardLocation;

public class MainCodeProcessor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        Messager messager = processingEnvironment.getMessager();
        Elements elementsUtils = processingEnvironment.getElementUtils();
        Types typeUtil = processingEnvironment.getTypeUtils();
        ProcessorUtil.init(messager, elementsUtils, typeUtil);
    }

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet();
        set.add("com.okreduce.annotation.OkGet");
        set.add("com.okreduce.annotation.OkPost");
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return false;
        }

        Iterator<? extends TypeElement> it = set.iterator();
        Set<Element> annoSet = new HashSet();
        while (it.hasNext()) {
            TypeElement element = it.next();
            Set<TypeElement> apiTypeSet = ElementFilter.typesIn(roundEnvironment.getElementsAnnotatedWith(element));

            if (apiTypeSet == null || apiTypeSet.isEmpty()) {
                continue;
            }
            Iterator<? extends TypeElement> apiIt = apiTypeSet.iterator();
            while (apiIt.hasNext()) {
                annoSet.add(apiIt.next());
            }
        }

        String packageName = getPackageName();
        boolean isAndroidPlatform = isAndroidPlatform();
        MainCodeElementManager elementManager = new MainCodeElementManager(packageName, isAndroidPlatform);
        elementManager.init(annoSet);

        return elementManager.writeTo(processingEnv.getFiler(), null);
    }

    private String getPackageName() {
        String packageName = processingEnv.getOptions().get("apiPackageName");
        if (packageName == null || packageName.isEmpty()) {
            packageName = "com.okreduce.api";
        }
        return packageName;
    }

    private boolean isAndroidPlatform() {
        File file = null;
        try {
            FileObject fileObject = processingEnv.getFiler().getResource(StandardLocation.SOURCE_OUTPUT, "ok", "ok");
            //D:\Project\OkHttpAnno-master\app\build\generated\source\apt\debug\ok\ok
            file = new File(fileObject.toUri().getPath());
            for (int i = 0; i < 7; i++) {
                file = file.getParentFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file == null || file.isFile()) {
            return false;
        }
        return findAndroidKeyword(file.listFiles());
    }

    private boolean findAndroidKeyword(File[] files) {
        if (files == null || files.length == 0) {
            return false;
        }
        for (File file : files) {
            if (file.isFile() && "AndroidManifest.xml".equals(file.getName())) {
                return true;
            } else if (file.isFile() && file.getName().endsWith(".gradle") && checkGradleFile(file)) {
                return true;
            } else if (file.isDirectory() && "src".equals(file.getName()) || "main".equals(file.getName())) {
                boolean flag = findAndroidKeyword(file.listFiles());
                if (flag) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkGradleFile(File file) {
        String content = StreamUtil.readFileString(file);
        if (content == null) {
            return false;
        }
        int index = content.indexOf("apply plugin: 'com.android.application'");
        if (index < 0) {
            index = content.indexOf("apply plugin: 'com.android.library'");
        }
        if (index >= 0) {
            return true;
        }
        return false;
    }

}
