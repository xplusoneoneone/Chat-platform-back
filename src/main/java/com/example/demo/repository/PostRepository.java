package com.example.demo.repository;

import com.example.demo.entity.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 动态数据访问层
 */
@Repository
public class PostRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 保存动态
     */
    public Post save(Post post) {
        String sql = "INSERT INTO post (user_id, content, `like`, create_time) VALUES (?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, post.getUserId());
            ps.setString(2, post.getContent());
            ps.setInt(3, post.getLike() != null ? post.getLike() : 0);
            ps.setObject(4, post.getCreateTime() != null ? post.getCreateTime() : LocalDateTime.now());
            return ps;
        }, keyHolder);
        
        Long id = keyHolder.getKey().longValue();
        post.setId(id);
        return post;
    }

    /**
     * 根据ID查找动态
     */
    public Optional<Post> findById(Long id) {
        String sql = "SELECT id, user_id as userId, content, `like`, create_time as createTime " +
                     "FROM post WHERE id = ?";
        try {
            Post post = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(Post.class), id);
            return Optional.ofNullable(post);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据用户ID查找动态列表
     */
    public List<Post> findByUserId(Long userId) {
        String sql = "SELECT id, user_id as userId, content, `like`, create_time as createTime " +
                     "FROM post WHERE user_id = ? ORDER BY create_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Post.class), userId);
    }

    /**
     * 查找所有动态列表（按时间倒序）
     */
    public List<Post> findAll() {
        String sql = "SELECT id, user_id as userId, content, `like`, create_time as createTime " +
                     "FROM post ORDER BY create_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Post.class));
    }

    /**
     * 查找好友的动态列表（分页，按时间倒序）
     * @param userId 用户ID
     * @param friendIds 好友ID列表
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 动态列表
     */
    public List<Post> findFriendPosts(Long userId, List<Long> friendIds, Integer offset, Integer limit) {
        if (friendIds == null || friendIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 构建IN子句的占位符
        String placeholders = friendIds.stream()
                .map(id -> "?")
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        
        String sql = "SELECT id, user_id as userId, content, `like`, create_time as createTime " +
                     "FROM post WHERE user_id IN (" + placeholders + ") " +
                     "ORDER BY create_time DESC LIMIT ? OFFSET ?";
        
        List<Object> params = new ArrayList<>(friendIds);
        params.add(limit);
        params.add(offset);
        
        return jdbcTemplate.query(sql, params.toArray(), new BeanPropertyRowMapper<>(Post.class));
    }

    /**
     * 查找非好友的动态列表（分页，按时间倒序）
     * @param userId 用户ID（排除自己的动态）
     * @param friendIds 好友ID列表（排除好友的动态）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 动态列表
     */
    public List<Post> findNonFriendPosts(Long userId, List<Long> friendIds, Integer offset, Integer limit) {
        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        StringBuilder sql = new StringBuilder(
            "SELECT id, user_id as userId, content, `like`, create_time as createTime " +
            "FROM post WHERE user_id != ?"
        );
        
        // 排除好友的动态
        if (friendIds != null && !friendIds.isEmpty()) {
            String placeholders = friendIds.stream()
                    .map(id -> "?")
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            sql.append(" AND user_id NOT IN (").append(placeholders).append(")");
            params.addAll(friendIds);
        }
        
        sql.append(" ORDER BY create_time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        return jdbcTemplate.query(sql.toString(), params.toArray(), new BeanPropertyRowMapper<>(Post.class));
    }

    /**
     * 统计好友动态总数
     */
    public Long countFriendPosts(List<Long> friendIds) {
        if (friendIds == null || friendIds.isEmpty()) {
            return 0L;
        }
        
        String placeholders = friendIds.stream()
                .map(id -> "?")
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        
        String sql = "SELECT COUNT(*) FROM post WHERE user_id IN (" + placeholders + ")";
        return jdbcTemplate.queryForObject(sql, Long.class, friendIds.toArray());
    }

    /**
     * 统计非好友动态总数
     */
    public Long countNonFriendPosts(Long userId, List<Long> friendIds) {
        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM post WHERE user_id != ?");
        
        if (friendIds != null && !friendIds.isEmpty()) {
            String placeholders = friendIds.stream()
                    .map(id -> "?")
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            sql.append(" AND user_id NOT IN (").append(placeholders).append(")");
            params.addAll(friendIds);
        }
        
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }
}

