package com.java4all.entity;

import java.math.BigDecimal;

/**
 * @author wangzhongxiang
 * @date 2019年08月27日 11:15:16
 */
public class User {

    private String id;

    private String name;

    private Integer age;

    private String province;

    private String city;

    private String address;

    private String hobby;

    private BigDecimal money;

    private String school;

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", address='" + address + '\'' +
                ", hobby='" + hobby + '\'' +
                ", money=" + money +
                ", school='" + school + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }
}
