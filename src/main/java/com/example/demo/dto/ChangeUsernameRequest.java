package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改用户名请求DTO
 */
public class ChangeUsernameRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "新用户名不能为空")
    @Size(min = 3, max = 50, message = "新用户名长度必须在3-50个字符之间")
    private String newUsername;

    public ChangeUsernameRequest() {
    }

    public ChangeUsernameRequest(String username, String password, String newUsername) {
        this.username = username;
        this.password = password;
        this.newUsername = newUsername;
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

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
}

