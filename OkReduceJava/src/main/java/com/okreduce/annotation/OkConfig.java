package com.okreduce.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface OkConfig {
    int connectout() default -1;
    int readout() default -1;
    int writeout() default -1;
    int retry() default 0;
    boolean debug() default false;
    String log() default "";
}
