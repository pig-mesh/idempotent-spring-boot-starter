package com.java4all.aspect;

import com.java4all.annotation.Idempotent;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author 汪忠祥
 */
@Aspect
@Component
public class IdempotentAspect {

    @Autowired
    private Redisson redisson;


    @Pointcut("@annotation(com.java4all.annotation.Idempotent)")
    public void pointCut(){}

    @Before("pointCut()")
    public void beforePointCut(JoinPoint joinPoint){
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        if(method.isAnnotationPresent(Idempotent.class)){
            Idempotent idempotent = method.getAnnotation(Idempotent.class);
            boolean isIdempotent = idempotent.idempotent();
            if(!isIdempotent){
                return;
            }

            redisson.getMapCache("");

            long expireTime = idempotent.expireTime();

            
        }


        String url = request.getRequestURL().toString();
        String args = Arrays.toString(joinPoint.getArgs());

        String info = url + args;



        System.out.println("aaaaaa");

        //获取ip
        //获取参数
        //获取系统时间

        //每一个请求进来
        //md5(Ip+请求参数)=  000000aaaaa
        // a存入redis 加有效期   (000000aaaaa,time)
        //。。。走业务流程

        //第二个请求进来
        //md5(Ip+请求参数)=  000000aaaaa
        //去redis查000000aaaaa
        //有  踢回去，重复请求
        //没有  没有，放行，存入进去




        //请求开始时，存入标记
        //请求结束时，删除标记
        //之间来的请求，都校验标记，存在就踢回去。防止一个请求时间过长，还未处理完下一个又进来，还是没有防止住


    }
}
