package com.java4all.aspect;

import com.java4all.exception.IdempotentException;
import com.java4all.annotation.Idempotent;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
 * @email 1186355422@qq.com
 */
@Aspect
@Component
public class IdempotentAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentAspect.class);
    private ThreadLocal<Map<String,Object>> threadLocal = new ThreadLocal();
    private static final String RMAPCACHE_KEY = "idempotent";
    private static final String KEY = "key";
    private static final String DELKEY = "delKey";

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
        String key = url + argString;

        long expireTime = idempotent.expireTime();
        String info = idempotent.info();
        TimeUnit timeUnit = idempotent.timeUnit();
        boolean delKey = idempotent.delKey();

        //do not need check null
        RMapCache<String, Object> rMapCache = redisson.getMapCache(RMAPCACHE_KEY);

        String value = LocalDateTime.now().toString().replace("T", " ");
        Object v1;
        if (null != rMapCache.get(key)){
            throw new IdempotentException("[idempotent]:"+info);
        }
        synchronized (this){
            v1 = rMapCache.putIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
            if(null != v1){
                throw new IdempotentException("[idempotent]:"+info);
            }else {
                LOGGER.info("[idempotent]:has stored key={},value={},expireTime={}{},now={}",key,value,expireTime,timeUnit,LocalDateTime.now().toString());
            }
        }

        if(CollectionUtils.isEmpty(threadLocal.get())){
            Map<String, Object> map = new HashMap<>(2);
            map.put(KEY,key);
            map.put(DELKEY,delKey);
            threadLocal.set(map);
        }

    }

    @After("pointCut()")
    public void afterPointCut(JoinPoint joinPoint){
        Map<String,Object> map = threadLocal.get();
        if(CollectionUtils.isEmpty(map)){
            return;
        }

        RMapCache<Object, Object> mapCache = redisson.getMapCache(RMAPCACHE_KEY);
        if(mapCache.size() == 0){
            return;
        }

        String key = map.get(KEY).toString();
        boolean delKey = (boolean)map.get(DELKEY);

        if(delKey){
            mapCache.fastRemove(key);
            LOGGER.info("[idempotent]:has removed key={}",key);
        }
    }
}
