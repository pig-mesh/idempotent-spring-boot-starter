package com.java4all.controller;

import com.java4all.annotation.Idempotent;
import com.java4all.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangzhongxiang
 */
@RestController
@RequestMapping("user")
public class UserController {


    @Idempotent(idempotent = true,expireTime = 3)
    @GetMapping(value = "add")
    public String add(User user){
        System.out.println(user.toString());
        return "添加成功";
    }

    @RequestMapping(value ="test")
    public String  test(String name){
        return name;
    }
}
