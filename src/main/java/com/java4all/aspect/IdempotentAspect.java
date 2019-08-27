package com.java4all.aspect;

import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author 汪忠祥
 */
@Aspect
@Component
public class IdempotentAspect {


    @Pointcut("@annotation(com.java4all.annotation.Idempotent)")
    public void pointCut(){}

    @Before("pointCut()")
    public void beforePointCut(JoinPoint joinPoint){
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        String ip = request.getRemoteAddr();
        String url = request.getRequestURL().toString();
        String args = Arrays.toString(joinPoint.getArgs());

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
