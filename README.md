# idempotent 幂等处理方案


是对原有 [idempotent](https://github.com/it4alla/idempotent) 代码重构和功能增强。

非常感谢 idempotent 作者的分享。

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

- 1. 引入依赖

| 版本 | 支持 |
|-------|--|
| 0.3.0 | 适配 SpringBoot3.x |
| 0.1.0 | 适配 SpringBoot2.x |

```java
<dependency>
    <groupId>com.pig4cloud.plugin</groupId>
    <artifactId>idempotent-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>

- 使用快照

<dependency>
    <groupId>com.pig4cloud.plugin</groupId>
    <artifactId>idempotent-spring-boot-starter</artifactId>
    <version>0.1.1-SNAPSHOT</version>
</dependency>

<repositories>
  <repository>
      <id>snapshots</id>
      <name>Excel Snapshots</name>
      <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
      <releases>
          <enabled>false</enabled>
      </releases>
  </repository>
</repositories> 
```



- 2. 配置 redis 链接相关信息

```yaml
spring:
  redis:
    host: 127.0.0.1
    port: 6379
```

理论是支持 [redisson-spring-boot-starter](https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter) 全部配置


- 3. 接口设置注解

```java
@Idempotent(key = "#demo.username", expireTime = 3, info = "请勿重复查询")
@GetMapping("/test")
public String test(Demo demo) {
    return "success";
}
```


### idempotent 注解 配置详细说明


- 1. 幂等操作的唯一标识，使用spring el表达式 用#来引用方法参数 。 可为空则取 当前 url + args 做表示
    
```java
String key();
```


- 2. 有效期 默认：1 有效期要大于程序执行时间，否则请求还是可能会进来

```java
	int expireTime() default 1;
```

- 3. 时间单位 默认：s （秒）

```java
TimeUnit timeUnit() default TimeUnit.SECONDS;
```

- 4. 幂等失败提示信息，可自定义

```java
String info() default "重复请求，请稍后重试";
```

- 5. 是否在业务完成后删除key true:删除 false:不删除

```java
boolean delKey() default false;
```

#### 微信群

![](https://i.loli.net/2020/09/25/SsWqJt2H157VbfI.png)
