package com.java4all.controller;

import com.java4all.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author ITyunqing
 * @email 1186355422@qq.com
 */
@Service("userServiceImpl")
public class UserServiceImpl implements UserService{
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public void add(User user) {
        try {
            Thread.sleep(1*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("添加用户成功");
    }
}
