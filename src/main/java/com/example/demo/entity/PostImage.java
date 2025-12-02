package com.example.demo.entity;

import java.time.LocalDateTime;

/**
 * 动态图片实体类
 */
public class PostImage {
    private Long id;
    private Long postId;
    private String imagePath;
    private Integer sortOrder;
    private LocalDateTime createTime;

    public PostImage() {
    }

    public PostImage(Long id, Long postId, String imagePath, Integer sortOrder, LocalDateTime createTime) {
        this.id = id;
        this.postId = postId;
        this.imagePath = imagePath;
        this.sortOrder = sortOrder;
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

