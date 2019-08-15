package com.java4all.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author IT云清
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Idempotent {


    /**
     * 是否幂等
     * 0：非幂等
     * 1：幂等
     * @return
     */
    int isIdempotent() default 0;

    /**
     * 周期
     * 单位：s
     * @return
     */
    long expireTime() default 1;
}
