package com.example.demo.entity;

import java.time.LocalDateTime;

/**
 * 动态实体类
 */
public class Post {
    private Long id;
    private Long userId;
    private String content;
    private Integer like;
    private LocalDateTime createTime;

    public Post() {
    }

    public Post(Long id, Long userId, String content, Integer like, LocalDateTime createTime) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.like = like;
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

