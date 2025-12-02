package com.example.demo.repository;

import com.example.demo.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 评论数据访问层
 */
@Repository
public class CommentRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 保存评论
     */
    public Comment save(Comment comment) {
        String sql = "INSERT INTO comment (post_id, user_id, parent_id, content, like_count, create_time) VALUES (?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, comment.getPostId());
            ps.setLong(2, comment.getUserId());
            ps.setObject(3, comment.getParentId());
            ps.setString(4, comment.getContent());
            ps.setInt(5, comment.getLikeCount() != null ? comment.getLikeCount() : 0);
            ps.setObject(6, comment.getCreateTime() != null ? comment.getCreateTime() : LocalDateTime.now());
            return ps;
        }, keyHolder);
        
        Long id = keyHolder.getKey().longValue();
        comment.setId(id);
        return comment;
    }

    /**
     * 根据ID查找评论
     */
    public Optional<Comment> findById(Long id) {
        String sql = "SELECT id, post_id as postId, user_id as userId, parent_id as parentId, content, like_count as likeCount, create_time as createTime " +
                     "FROM comment WHERE id = ?";
        try {
            Comment comment = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(Comment.class), id);
            return Optional.ofNullable(comment);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据帖子ID查找所有评论（按时间正序，顶级评论在前）
     */
    public List<Comment> findByPostId(Long postId) {
        String sql = "SELECT id, post_id as postId, user_id as userId, parent_id as parentId, content, like_count as likeCount, create_time as createTime " +
                     "FROM comment WHERE post_id = ? ORDER BY create_time ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Comment.class), postId);
    }

    /**
     * 根据父评论ID查找回复列表（按时间正序）
     */
    public List<Comment> findByParentId(Long parentId) {
        String sql = "SELECT id, post_id as postId, user_id as userId, parent_id as parentId, content, like_count as likeCount, create_time as createTime " +
                     "FROM comment WHERE parent_id = ? ORDER BY create_time ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Comment.class), parentId);
    }

    /**
     * 检查用户是否已点赞该评论
     */
    public boolean existsLike(Long commentId, Long userId) {
        String sql = "SELECT COUNT(*) FROM comment_like WHERE comment_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, commentId, userId);
        return count != null && count > 0;
    }

    /**
     * 添加点赞记录
     */
    public void addLike(Long commentId, Long userId) {
        String sql = "INSERT INTO comment_like (comment_id, user_id, create_time) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, commentId, userId, LocalDateTime.now());
    }

    /**
     * 删除点赞记录
     */
    public void removeLike(Long commentId, Long userId) {
        String sql = "DELETE FROM comment_like WHERE comment_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, commentId, userId);
    }

    /**
     * 增加评论点赞数
     */
    public void incrementLikeCount(Long commentId) {
        String sql = "UPDATE comment SET like_count = like_count + 1 WHERE id = ?";
        jdbcTemplate.update(sql, commentId);
    }

    /**
     * 减少评论点赞数
     */
    public void decrementLikeCount(Long commentId) {
        String sql = "UPDATE comment SET like_count = GREATEST(like_count - 1, 0) WHERE id = ?";
        jdbcTemplate.update(sql, commentId);
    }
}

