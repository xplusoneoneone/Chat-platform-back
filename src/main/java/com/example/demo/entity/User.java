package com.example.demo.entity;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
public class User {
    private Long id;
    private String username;
    private String password;
    private String avatar;
    private String sex;
    private String signature;
    private String location;
    private Integer isLogging;
    private LocalDateTime createTime;

    public User() {
    }

    public User(Long id, String username, String password, String avatar, String sex, String signature, String location, Integer isLogging, LocalDateTime createTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.avatar = avatar;
        this.sex = sex;
        this.signature = signature;
        this.location = location;
        this.isLogging = isLogging;
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getIsLogging() {
        return isLogging;
    }

    public void setIsLogging(Integer isLogging) {
        this.isLogging = isLogging;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

