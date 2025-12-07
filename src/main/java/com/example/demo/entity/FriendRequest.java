package com.example.demo.entity;

import java.time.LocalDateTime;

/**
 * 好友申请实体类
 */
public class FriendRequest {
    private Long id;
    private Long requesterId;  // 申请者ID
    private Long receiverId;    // 接收者ID
    private String status;       // 状态：pending(待处理), accepted(已同意), rejected(已拒绝)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public FriendRequest() {
    }

    public FriendRequest(Long id, Long requesterId, Long receiverId, String status, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}

