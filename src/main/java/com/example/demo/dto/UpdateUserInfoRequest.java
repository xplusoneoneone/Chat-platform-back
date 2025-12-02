package com.example.demo.dto;

/**
 * 更新用户信息请求DTO
 * 所有字段都是可选的，只更新传入的字段
 */
public class UpdateUserInfoRequest {
    
    private Long userId;
    private String username;
    private String avatar;
    private String signature;
    private String sex;

    public UpdateUserInfoRequest() {
    }

    public UpdateUserInfoRequest(Long userId, String username, String avatar, String signature, String sex) {
        this.userId = userId;
        this.username = username;
        this.avatar = avatar;
        this.signature = signature;
        this.sex = sex;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}

