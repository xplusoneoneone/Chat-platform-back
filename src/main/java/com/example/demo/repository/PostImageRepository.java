/*
 * @Author: 徐佳德 1404577549@qq.com
 * @Date: 2025-11-30 15:53:27
 * @LastEditors: 徐佳德 1404577549@qq.com
 * @LastEditTime: 2025-12-01 11:09:34
 * @FilePath: \chat-platform\demo\src\main\java\com\example\demo\repository\PostImageRepository.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.example.demo.repository;

import com.example.demo.entity.PostImage;
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

/**
 * 动态图片数据访问层
 */
@Repository
public class PostImageRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 保存图片记录
     */
    public PostImage save(PostImage postImage) {
        String sql = "INSERT INTO post_image (post_id, image_path, sort_order, create_time) VALUES (?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, postImage.getPostId());
            ps.setString(2, postImage.getImagePath());
            ps.setInt(3, postImage.getSortOrder() != null ? postImage.getSortOrder() : 0);
            ps.setObject(4, postImage.getCreateTime() != null ? postImage.getCreateTime() : LocalDateTime.now());
            return ps;
        }, keyHolder);
        
        Long id = keyHolder.getKey().longValue();
        postImage.setId(id);
        return postImage;
    }

    /**
     * 批量保存图片记录
     */
    public void saveBatch(List<PostImage> postImages) {
        String sql = "INSERT INTO post_image (post_id, image_path, sort_order, create_time) VALUES (?, ?, ?, ?)";
        
        jdbcTemplate.batchUpdate(sql, postImages, postImages.size(),
            (ps, postImage) -> {
                ps.setLong(1, postImage.getPostId());
                ps.setString(2, postImage.getImagePath());
                ps.setInt(3, postImage.getSortOrder() != null ? postImage.getSortOrder() : 0);
                ps.setObject(4, postImage.getCreateTime() != null ? postImage.getCreateTime() : LocalDateTime.now());
            });
    }

    /**
     * 根据动态ID查找所有图片
     */
    public List<PostImage> findByPostId(Long postId) {
        String sql = "SELECT id, post_id as postId, image_path as imagePath, sort_order as sortOrder, create_time as createTime " +
                     "FROM post_image WHERE post_id = ? ORDER BY sort_order ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PostImage.class), postId);
    }
}

