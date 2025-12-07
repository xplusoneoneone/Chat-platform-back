package com.example.demo.service;

import com.example.demo.dto.ChangePasswordRequest;
import com.example.demo.dto.ChangeUsernameRequest;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UpdateUserInfoRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务层
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户注册
     * @param request 注册请求
     * @return 注册成功后的用户信息（不包含密码）
     */
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        // 使用BCrypt加密密码
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAvatar("");
        user.setSex("男");
        user.setLocation(null);
        user.setCreateTime(LocalDateTime.now());
        user.setIsLogging(0);
        // 保存用户
        return userRepository.save(user);
    }

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 根据ID查找用户
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录成功后的用户信息（不包含密码）
     */
    public User login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 更新登录状态为在线
        userRepository.updateLoginStatus(user.getId(), 1);

        // 重新查询用户信息以获取更新后的状态
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 用户退出登录
     * @param userId 用户ID
     */
    public void logout(Long userId) {
        // 验证用户是否存在
        if (userRepository.findById(userId).isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        // 更新登录状态为离线
        userRepository.updateLoginStatus(userId, 0);
    }

    /**
     * 修改密码
     * @param request 修改密码请求
     */
    public void changePassword(ChangePasswordRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 检查新密码不能与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        // 加密新密码
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());

        // 更新密码
        userRepository.updatePassword(user.getId(), encodedNewPassword);
    }

    /**
     * 修改用户名
     * @param request 修改用户名请求
     * @return 更新后的用户信息
     */
    public User changeUsername(ChangeUsernameRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 检查新用户名不能与旧用户名相同
        if (request.getUsername().equals(request.getNewUsername())) {
            throw new RuntimeException("新用户名不能与当前用户名相同");
        }

        // 检查新用户名是否已存在
        if (userRepository.findByUsername(request.getNewUsername()).isPresent()) {
            throw new RuntimeException("新用户名已被使用");
        }

        // 更新用户名
        userRepository.updateUsername(user.getId(), request.getNewUsername());

        // 返回更新后的用户信息
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 修改用户头像
     * @param userId 用户ID
     * @param avatar 头像路径
     * @return 更新后的用户信息
     */
    public User updateAvatar(Long userId, String avatar) {
        // 验证用户是否存在
        if (userRepository.findById(userId).isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        // 更新头像
        userRepository.updateAvatar(userId, avatar);

        // 返回更新后的用户信息
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    /**
     * 更新用户信息（只更新传入的字段）
     * @param request 更新用户信息请求
     * @return 更新后的用户信息
     */
    public User updateUserInfo(UpdateUserInfoRequest request) {
        // 验证用户是否存在
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 如果更新用户名，需要检查新用户名是否已被使用
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            // 检查新用户名是否与当前用户名相同
            if (!request.getUsername().equals(user.getUsername())) {
                // 检查新用户名是否已被其他用户使用
                userRepository.findByUsername(request.getUsername()).ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(request.getUserId())) {
                        throw new RuntimeException("用户名已被使用");
                    }
                });
            }
        }

        // 更新用户信息（只更新非空字段）
        userRepository.updateUserInfo(
                request.getUserId(),
                request.getUsername(),
                request.getAvatar(),
                request.getSignature(),
                request.getSex(),
                request.getLocation()
        );

        // 返回更新后的用户信息
        return userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}

