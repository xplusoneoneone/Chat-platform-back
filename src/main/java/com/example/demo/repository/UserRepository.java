package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public class UserRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password, avatar, sex, signature, location, is_logging as isLogging, create_time as createTime " +
                     "FROM user WHERE username = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(User.class), username);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        String sql = "SELECT id, username, password, avatar, sex, signature, location, is_logging as isLogging, create_time as createTime " +
                     "FROM user WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(User.class), id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 保存用户（注册）
     */
    public User save(User user) {
        String sql = "INSERT INTO user (username, password, avatar, sex, signature, location, create_time, is_logging) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getAvatar() != null ? user.getAvatar() : "https://xxx.com/default-avatar.png");
            ps.setString(4, user.getSex());
            ps.setString(5, user.getSignature());
            ps.setString(6, user.getLocation());
            ps.setObject(7, user.getCreateTime() != null ? user.getCreateTime() : LocalDateTime.now());
            ps.setInt(8, user.getIsLogging() != null ? user.getIsLogging() : 0);
            return ps;
        }, keyHolder);
        
        Long id = keyHolder.getKey().longValue();
        user.setId(id);
        return user;
    }

    /**
     * 更新用户密码
     */
    public void updatePassword(Long userId, String newPassword) {
        String sql = "UPDATE user SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql, newPassword, userId);
    }

    /**
     * 更新用户名
     */
    public void updateUsername(Long userId, String newUsername) {
        String sql = "UPDATE user SET username = ? WHERE id = ?";
        jdbcTemplate.update(sql, newUsername, userId);
    }

    /**
     * 更新用户头像
     */
    public void updateAvatar(Long userId, String avatar) {
        String sql = "UPDATE user SET avatar = ? WHERE id = ?";
        jdbcTemplate.update(sql, avatar, userId);
    }

    /**
     * 更新用户信息（只更新非空字段）
     */
    public void updateUserInfo(Long userId, String username, String avatar, String signature, String sex, String location) {
        StringBuilder sql = new StringBuilder("UPDATE user SET ");
        java.util.List<Object> params = new java.util.ArrayList<>();
        boolean hasUpdate = false;

        if (username != null && !username.trim().isEmpty()) {
            sql.append("username = ?");
            params.add(username);
            hasUpdate = true;
        }

        if (avatar != null) {
            if (hasUpdate) {
                sql.append(", ");
            }
            sql.append("avatar = ?");
            params.add(avatar);
            hasUpdate = true;
        }

        if (signature != null) {
            if (hasUpdate) {
                sql.append(", ");
            }
            sql.append("signature = ?");
            params.add(signature);
            hasUpdate = true;
        }

        if (sex != null) {
            if (hasUpdate) {
                sql.append(", ");
            }
            sql.append("sex = ?");
            params.add(sex);
            hasUpdate = true;
        }

        if (location != null) {
            if (hasUpdate) {
                sql.append(", ");
            }
            sql.append("location = ?");
            params.add(location);
            hasUpdate = true;
        }

        if (!hasUpdate) {
            // 如果没有要更新的字段，直接返回
            return;
        }

        sql.append(" WHERE id = ?");
        params.add(userId);

        jdbcTemplate.update(sql.toString(), params.toArray());
    }

    /**
     * 更新用户登录状态
     */
    public void updateLoginStatus(Long userId, Integer isLogging) {
        String sql = "UPDATE user SET is_logging = ? WHERE id = ?";
        jdbcTemplate.update(sql, isLogging, userId);
    }
}

