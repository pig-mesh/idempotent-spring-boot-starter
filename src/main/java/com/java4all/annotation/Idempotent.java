package com.java4all.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * @author ITyunqing
 * @email 1186355422@qq.com
 * you can use it on your interface in controller like this :
 *     @Idempotent(idempotent = true,expireTime = 6,timeUnit = TimeUnit.SECONDS,info = "请勿重复添加用户")
 *     @GetMapping(value = "add")
 *     public String add(User user){
 *          userServiceImpl.add(user);
 *         return "success";
 *     }
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
    boolean isIdempotent() default false;

    /**
     * 有效期
     * 默认：1
     * 有效期要大于程序执行时间，否则请求还是可能会进来
     * @return
     */
    int expireTime() default 1;

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

    /**
     * 是否在业务完成后删除key
     * true:删除
     * false:不删除
     * @return
     */
    boolean delKey() default false;
}
