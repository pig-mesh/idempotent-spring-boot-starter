package com.java4all.controller;

import com.java4all.annotation.Idempotent;
import com.java4all.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangzhongxiang
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userServiceImpl;

    @Idempotent(idempotent = true,expireTime = 6,info = "请勿重复添加用户")
    @GetMapping(value = "add")
    public String add(User user){
        userServiceImpl.add(user);
        return "添加成功";
    }

    @RequestMapping(value ="test")
    public String  test(String name){
        return name;
    }
}
