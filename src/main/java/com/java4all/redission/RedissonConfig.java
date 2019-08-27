package com.java4all.redission;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @description: Redisson配置类
 * @author: yufangze
 */
@Configuration
public class RedissonConfig {

    @Value("${singleServerConfig.address}")
    private String address;

    @Value("${singleServerConfig.password}")
    private String password;

    @Value("${singleServerConfig.pingTimeout}")
    private int pingTimeout;

    @Value("${singleServerConfig.connectTimeout}")
    private int connectTimeout;

    @Value("${singleServerConfig.timeout}")
    private int timeout;

    @Value("${singleServerConfig.idleConnectionTimeout}")
    private int idleConnectionTimeout;

    @Value("${singleServerConfig.retryAttempts}")
    private int retryAttempts;

    @Value("${singleServerConfig.retryInterval}")
    private int retryInterval;

    @Value("${singleServerConfig.reconnectionTimeout}")
    private int reconnectionTimeout;

    @Value("${singleServerConfig.failedAttempts}")
    private int failedAttempts;

    @Value("${singleServerConfig.subscriptionsPerConnection}")
    private int subscriptionsPerConnection;

    @Value("${singleServerConfig.subscriptionConnectionMinimumIdleSize}")
    private int subscriptionConnectionMinimumIdleSize;

    @Value("${singleServerConfig.subscriptionConnectionPoolSize}")
    private int subscriptionConnectionPoolSize;

    @Value("${singleServerConfig.connectionMinimumIdleSize}")
    private int connectionMinimumIdleSize;

    @Value("${singleServerConfig.connectionPoolSize}")
    private int connectionPoolSize;


    @Bean(destroyMethod = "shutdown")
    public Redisson redisson() {
        Config config = new Config();
        config.useSingleServer().setAddress(address)
                .setPassword(password)
                .setIdleConnectionTimeout(idleConnectionTimeout)
                .setConnectTimeout(connectTimeout)
                .setTimeout(timeout)
                .setRetryAttempts(retryAttempts)
                .setRetryInterval(retryInterval)
                .setReconnectionTimeout(reconnectionTimeout)
                .setPingTimeout(pingTimeout)
                .setFailedAttempts(failedAttempts)
                .setSubscriptionsPerConnection(subscriptionsPerConnection)
                .setSubscriptionConnectionMinimumIdleSize(subscriptionConnectionMinimumIdleSize)
                .setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setConnectionPoolSize(connectionPoolSize);
        return (Redisson) Redisson.create(config);
    }

}
