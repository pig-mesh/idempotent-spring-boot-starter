package com.java4all.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author 汪忠祥
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Idempotent {


    /**
     * 是否做幂等处理
     * false：非幂等
     * true：幂等
     * @return
     */
    boolean idempotent() default false;

    /**
     * 有效期
     * 默认：1
     * @return
     */
    long expireTime() default 1;

    /**
     * 时间单位
     * 默认：s
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 提示信息，可自定义
     * @return
     */
    String info() default "重复请求，请稍后重试";
}
