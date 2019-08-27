package com.java4all.aspect;

import com.java4all.IdempotentException;
import com.java4all.annotation.Idempotent;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.Redisson;
import org.redisson.api.RMapCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author 汪忠祥
 */
@Aspect
@Component
public class IdempotentAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentAspect.class);
    private ThreadLocal threadLocal = new ThreadLocal();
    private static final String RMAPCACHE_KEY = "idempotent";

    @Autowired
    private Redisson redisson;


    @Pointcut("@annotation(com.java4all.annotation.Idempotent)")
    public void pointCut(){}

    @Before("pointCut()")
    public void beforePointCut(JoinPoint joinPoint)throws Exception{
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        if(!method.isAnnotationPresent(Idempotent.class)){
            return;
        }
        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        boolean isIdempotent = idempotent.idempotent();
        if(!isIdempotent){
            return;
        }

        String url = request.getRequestURL().toString();
        String argString  = Arrays.asList(joinPoint.getArgs()).toString();
        long expireTime = idempotent.expireTime();
        String info = idempotent.info();
        TimeUnit timeUnit = idempotent.timeUnit();
        String key = url + argString;
        RMapCache<String, Object> rMapCache = redisson.getMapCache(RMAPCACHE_KEY);
        if (null != rMapCache.get(key)){
            throw new IdempotentException("[idempotent]:"+info);
        }
        String value = LocalDateTime.now().toString().replace("T", " ");
        rMapCache.putIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
        threadLocal.set(key);

        LOGGER.info("[idempotent]:has stored key={},value={},expireTime={}{}",key,value,expireTime,timeUnit);
    }

    @After("pointCut()")
    public void afterPointCut(JoinPoint joinPoint){
        Object key = threadLocal.get();
        if(null == key){
            return;
        }
        RMapCache<Object, Object> mapCache = redisson.getMapCache(RMAPCACHE_KEY);
        if(null == mapCache){
            return;
        }
        //TODO 如何使用删除更好的优化
        mapCache.fastRemove(key);
        LOGGER.info("[idempotent]:has removed key={}",key);
    }
}
