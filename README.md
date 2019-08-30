# idempotent 幂等处理方案
An idempotent solution.
### 1.原理
1.请求开始前，根据key查询
查到结果：报错
未查到结果：存入key-value-expireTime
key=ip+url+args

2.请求结束后，直接删除key
不管key是否存在，直接删除
是否删除，可配置

3.expireTime过期时间，防止一个请求卡死，会一直阻塞，超过过期时间，自动删除
过期时间要大于业务执行时间，需要大概评估下;

4.此方案直接切的是接口请求层面。

5.过期时间需要大于业务执行时间，否则业务请求1进来还在执行中，前端未做遮罩，或者用户跳转页面后再回来做重复请求2，在业务层面上看，结果依旧是不符合预期的。

6.建议delKey = false。即使业务执行完，也不删除key，强制锁expireTime的时间。预防5的情况发生。

7.实现思路：同一个请求ip和接口，相同参数的请求，在expireTime内多次请求，只允许成功一次。

8.页面做遮罩，数据库层面的唯一索引，先查询再添加，等处理方式应该都处理下。

9.此注解只用于幂等，不用于锁，100个并发这种压测，会出现问题，在这种场景下也没有意义，实际中用户也不会出现1s或者3s内手动发送了50个或者100个重复请求,或者弱网下有100个重复请求；


### 2.使用
引入注解，切面，配置类，异常类，修改配置，直接在需要使用的接口上添加注解即可；
（后期会优化为jar）
使用如下：
```java
    @Idempotent(idempotent = true,expireTime = 3,timeUnit = TimeUnit.SECONDS,info = "请勿重复添加用户",delKey = false)
    @GetMapping(value = "add")
    public String add(User user){
        userServiceImpl.add(user);
        return "添加成功";
    }
```

### 3.测试
jmeter并发压测，或者charles弱网测试结果：

```java
2019-08-28 13:45:11.847  INFO 5468 --- [nio-7777-exec-4] com.java4all.aspect.IdempotentAspect     : [idempotent]:has stored key=http://localhost:7777/user/add[User{id='11', name='wang', age=26, province='陕西', city='商洛市', address='商南县', hobby='magic', money=100000.99, school='清华大学'}],value=2019-08-28 13:45:11.824,expireTime=6SECONDS
2019-08-28 13:45:12.160 ERROR 5468 --- [nio-7777-exec-5] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.reflect.UndeclaredThrowableException] with root cause

com.java4all.exception.IdempotentException: [idempotent]:请勿重复添加用户
	at com.java4all.aspect.IdempotentAspect.beforePointCut(IdempotentAspect.java:69) ~[classes/:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_65]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_65]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_65]
	at java.lang.reflect.Method.invoke(Method.java:497) ~[na:1.8.0_65]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:644) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:626) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.aspectj.AspectJMethodBeforeAdvice.before(AspectJMethodBeforeAdvice.java:44) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:55) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.aspectj.AspectJAfterAdvice.invoke(AspectJAfterAdvice.java:47) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:93) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:688) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at com.java4all.controller.UserController$$EnhancerBySpringCGLIB$$7c3364f2.add(<generated>) ~[classes/:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_65]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_65]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_65]
	at java.lang.reflect.Method.invoke(Method.java:497) ~[na:1.8.0_65]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:104) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:892) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:797) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1039) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:942) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1005) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:897) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:634) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:882) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:741) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[tomcat-embed-websocket-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:99) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:92) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.springframework.web.filter.HiddenHttpMethodFilter.doFilterInternal(HiddenHttpMethodFilter.java:93) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:200) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:490) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:139) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:408) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:853) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1587) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [na:1.8.0_65]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [na:1.8.0_65]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at java.lang.Thread.run(Thread.java:745) [na:1.8.0_65]

2019-08-28 13:45:12.502 ERROR 5468 --- [nio-7777-exec-6] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.reflect.UndeclaredThrowableException] with root cause

com.java4all.exception.IdempotentException: [idempotent]:请勿重复添加用户
	at com.java4all.aspect.IdempotentAspect.beforePointCut(IdempotentAspect.java:69) ~[classes/:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_65]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_65]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_65]
	at java.lang.reflect.Method.invoke(Method.java:497) ~[na:1.8.0_65]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs(AbstractAspectJAdvice.java:644) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod(AbstractAspectJAdvice.java:626) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.aspectj.AspectJMethodBeforeAdvice.before(AspectJMethodBeforeAdvice.java:44) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.framework.adapter.MethodBeforeAdviceInterceptor.invoke(MethodBeforeAdviceInterceptor.java:55) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.aspectj.AspectJAfterAdvice.invoke(AspectJAfterAdvice.java:47) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:175) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke(ExposeInvocationInterceptor.java:93) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:688) ~[spring-aop-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at com.java4all.controller.UserController$$EnhancerBySpringCGLIB$$7c3364f2.add(<generated>) ~[classes/:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_65]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_65]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_65]
	at java.lang.reflect.Method.invoke(Method.java:497) ~[na:1.8.0_65]
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:190) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:104) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:892) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:797) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1039) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:942) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1005) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:897) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:634) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:882) ~[spring-webmvc-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:741) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[tomcat-embed-websocket-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:99) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:92) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.springframework.web.filter.HiddenHttpMethodFilter.doFilterInternal(HiddenHttpMethodFilter.java:93) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:200) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:118) ~[spring-web-5.1.9.RELEASE.jar:5.1.9.RELEASE]
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:202) ~[tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:490) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:139) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:92) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:74) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:343) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:408) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:853) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1587) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142) [na:1.8.0_65]
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617) [na:1.8.0_65]
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61) [tomcat-embed-core-9.0.22.jar:9.0.22]
	at java.lang.Thread.run(Thread.java:745) [na:1.8.0_65]

2019-08-28 13:45:12.847  INFO 5468 --- [nio-7777-exec-4] com.java4all.controller.UserServiceImpl  : 添加用户成功
2019-08-28 13:45:12.864  INFO 5468 --- [nio-7777-exec-4] com.java4all.aspect.IdempotentAspect     : [idempotent]:has removed key=http://localhost:7777/user/add[User{id='11', name='wang', age=26, province='陕西', city='商洛市', address='商南县', hobby='magic', money=100000.99, school='清华大学'}]

```
