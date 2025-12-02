package com.example.demo.entity;

import java.time.LocalDateTime;

/**
 * 好友关系实体类
 */
public class Friend {
    private Long id;
    private Long userId;
    private Long friendId;
    private String remark;
    private LocalDateTime createTime;

    public Friend() {
    }

    public Friend(Long id, Long userId, Long friendId, String remark, LocalDateTime createTime) {
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.remark = remark;
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

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}

