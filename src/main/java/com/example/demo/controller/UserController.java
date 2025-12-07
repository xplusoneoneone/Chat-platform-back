package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.ChangeUsernameRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UpdateUserInfoRequest;
import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册接口
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // 调用服务层进行注册
            User user = userService.register(request);
            
            // 构建返回数据（不返回密码）
            Map<String, Object> userData = buildUserData(user);
            
            return ApiResponse.success("注册成功", userData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 用户登录接口
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        try {
            // 调用服务层进行登录验证
            User user = userService.login(request);
            
            // 构建返回数据（不返回密码）
            Map<String, Object> userData = buildUserData(user);
            
            return ApiResponse.success("登录成功", userData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码接口
     * @param request 修改密码请求
     * @return 修改结果
     */
    @PostMapping("/change-password")
    public ApiResponse<Object> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            // 调用服务层修改密码
            userService.changePassword(request);
            return ApiResponse.success("密码修改成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("密码修改失败：" + e.getMessage());
        }
    }

    /**
     * 修改用户名接口（已废弃，请使用 /update-info 接口）
     * @param request 修改用户名请求
     * @return 修改结果
     */
    @Deprecated
    @PostMapping("/change-username")
    public ApiResponse<Map<String, Object>> changeUsername(@Valid @RequestBody ChangeUsernameRequest request) {
        try {
            // 调用服务层修改用户名
            User user = userService.changeUsername(request);
            
            // 构建返回数据（不返回密码）
            Map<String, Object> userData = buildUserData(user);
            
            return ApiResponse.success("用户名修改成功", userData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("用户名修改失败：" + e.getMessage());
        }
    }

    /**
     * 修改用户头像接口（已废弃，请使用 /update-info 接口）
     * @param userId 用户ID
     * @param avatar 头像路径
     * @return 修改结果
     */
    @Deprecated
    @PostMapping("/update-avatar")
    public ApiResponse<Map<String, Object>> updateAvatar(
            @RequestParam("userId") Long userId,
            @RequestParam("avatar") String avatar) {
        try {
            // 调用服务层修改头像
            User user = userService.updateAvatar(userId, avatar);
            
            // 构建返回数据（不返回密码）
            Map<String, Object> userData = buildUserData(user);
            
            return ApiResponse.success("头像修改成功", userData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("头像修改失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息接口（统一接口，可更新姓名、头像、个性签名、性别）
     * 如果前端没有传值就保持不变，传了就改
     * @param request 更新用户信息请求
     * @return 更新结果
     */
    @PostMapping("/update-info")
    public ApiResponse<Map<String, Object>> updateUserInfo(@RequestBody UpdateUserInfoRequest request) {
        try {
            // 验证userId不能为空
            if (request.getUserId() == null) {
                return ApiResponse.error(400, "用户ID不能为空");
            }

            // 调用服务层更新用户信息
            User user = userService.updateUserInfo(request);
            
            // 构建返回数据（不返回密码）
            Map<String, Object> userData = buildUserData(user);
            
            return ApiResponse.success("用户信息更新成功", userData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("用户信息更新失败：" + e.getMessage());
        }
    }

    /**
     * 用户退出登录接口
     * @param userId 用户ID
     * @return 退出登录结果
     */
    @PostMapping("/logout")
    public ApiResponse<Object> logout(@RequestParam("userId") Long userId) {
        try {
            // 调用服务层退出登录
            userService.logout(userId);
            return ApiResponse.success("退出登录成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("退出登录失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户信息接口
     * @param userId 用户ID
     * @return 用户信息（姓名、头像、个性签名）
     */
    @GetMapping("/{userId}")
    public ApiResponse<Map<String, Object>> getUserInfo(@PathVariable("userId") Long userId) {
        try {
            // 调用服务层查找用户
            User user = userService.findById(userId);
            
            // 构建返回数据（只返回姓名、头像、个性签名）
            Map<String, Object> userInfo = buildUserInfo(user);
            
            return ApiResponse.success("获取用户信息成功", userInfo);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 构建用户数据（不包含密码）
     */
    private Map<String, Object> buildUserData(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("avatar", user.getAvatar());
        userData.put("sex", user.getSex());
        userData.put("location", user.getLocation());
        userData.put("signature", user.getSignature());
        userData.put("createTime", user.getCreateTime());
        return userData;
    }

    /**
     * 构建用户信息（只包含姓名、头像、个性签名）
     */
    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", user.getUsername());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("sex", user.getSex());
        userInfo.put("location", user.getLocation());
        userInfo.put("signature", user.getSignature());
        return userInfo;
    }
}

