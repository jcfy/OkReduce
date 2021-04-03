package com.okreduce.annotation;

import com.okreduce.constant.ContentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface OkPost {
    String uri() default "";

    String url() default "";

    //is backup url
    String[] urls() default {};

    ContentType type() default ContentType.FORM;
}
