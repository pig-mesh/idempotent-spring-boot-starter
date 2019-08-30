package com.java4all.redission;

import java.util.concurrent.TimeUnit;

import org.redisson.Redisson;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * redisson分布式锁
 * @author wangzhongxiang
 * @date 2019年04月02日 10:00:54
 */
@Component
public class RedissonDistributedLocker {
    private static final Logger logger = LoggerFactory.getLogger(RedissonDistributedLocker.class);

    @Autowired
    private Redisson redisson;

    /**
     * 获取锁，需要主动释放
     * @param lockKey
     * @return
     */
    public RLock lock(String lockKey){
        RLock lock = redisson.getLock(lockKey);
        lock.lock();
        logger.info("【Redisson lock】success to acquire lock for [ "+lockKey+" ]");
        return lock;
    }

    /**
     * 获取锁，如果没有主动调用unlock解锁，expireTime后会自动释放
     * @param lockKey
     * @param expireTime 如果没有调用unlock解锁，expireTime 后自动释放
     * @param timeUnit 时间单位
     * @return
     */
    public RLock lock(String lockKey,Integer expireTime,TimeUnit timeUnit){
        RLock lock = redisson.getLock(lockKey);
        lock.lock(expireTime, timeUnit);
        logger.info("【Redisson lock】success to acquire lock for [ "+lockKey+" ],expire time:"+expireTime+timeUnit);
        return lock;
    }

    /**
     * 获取公平锁，如果没有主动调用unlock解锁，expireTime后会自动释放
     * @param lockKey
     * @param expireTime 如果没有调用unlock解锁，expireTime 后自动释放
     * @param timeUnit 时间单位
     * @return
     */
    public RLock fairLock(String lockKey,Integer expireTime,TimeUnit timeUnit){
        RLock fairLock = redisson.getFairLock(lockKey);
        fairLock.lock(expireTime, timeUnit);
        logger.info("【Redisson lock】success to acquire fair lock for [ "+lockKey+" ],expire time:"+expireTime+timeUnit);
        return fairLock;
    }

    /**
     * 获取锁 尝试加锁，最多等待waitTime 的时间，加锁expireTime 后自动释放
     * @param lockKey
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @param waitTime 加锁等待
     * @return
     */
    public boolean tryLock(String lockKey, Integer expireTime,TimeUnit timeUnit,Integer waitTime) {
        RLock lock = redisson.getLock(lockKey);
        try {
            logger.info("【Redisson lock】success to acquire lock for [ "+lockKey+" ],expire time:"+expireTime+timeUnit);
            return lock.tryLock(waitTime, expireTime, timeUnit);
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 联锁 获取一组锁，一组资源的锁，当作一个锁
     * @param expireTime
     * @param timeUnit 如果没有调用unlock解锁，expireTime 后自动释放
     * @param lockKey 时间单位
     * @return
     */
    public RedissonMultiLock multiLock(Integer expireTime,TimeUnit timeUnit,String ...lockKey){
        RLock [] rLocks = new RLock[lockKey.length];
        for(int i = 0,length = lockKey.length; i < length ;i ++){
            RLock lock = redisson.getLock(lockKey[i]);
            rLocks[i] = lock;
        }
        RedissonMultiLock multiLock = new RedissonMultiLock(rLocks);
        multiLock.lock(expireTime,timeUnit);
        logger.info("【Redisson lock】success to acquire multiLock for [ "+lockKey+" ],expire time:"+expireTime+timeUnit);
        return multiLock;
    }

    /**
     * 释放锁
     * @param lockKey
     */
    public void unLock(String lockKey){
        RLock lock = redisson.getLock(lockKey);
        lock.unlock();
        logger.info("【Redisson lock】success to release lock for [ "+lockKey+" ]");
    }

    /**
     * 释放锁
     * @param rLock
     */
    public void unLock(RLock rLock) {
        rLock.unlock();
        logger.info("【Redisson lock】success to release lock for [ "+rLock.getName()+" ]");
    }

    /**
     * 释放联锁
     * @param multiLock
     */
    public void unLockMultiLock(RedissonMultiLock multiLock) {
        multiLock.unlock();
        logger.info("【Redisson lock】success to release lock");
    }
}
