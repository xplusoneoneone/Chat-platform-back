package com.example.demo.service;

import com.example.demo.entity.Friend;
import com.example.demo.repository.FriendRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 好友服务层
 */
@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 添加好友（双向关系）
     * @param userId 用户ID
     * @param friendId 好友ID
     */
    public void addFriend(Long userId, Long friendId) {
        // 验证不能添加自己为好友
        if (userId.equals(friendId)) {
            throw new RuntimeException("不能添加自己为好友");
        }

        // 验证用户是否存在
        if (userRepository.findById(userId).isEmpty()) {
            throw new RuntimeException("用户不存在");
        }

        // 验证好友是否存在
        if (userRepository.findById(friendId).isEmpty()) {
            throw new RuntimeException("要添加的好友不存在");
        }

        // 检查是否已经是好友
        if (friendRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new RuntimeException("已经是好友关系");
        }

        // 创建双向好友关系
        LocalDateTime now = LocalDateTime.now();

        // 用户 -> 好友
        Friend friend1 = new Friend();
        friend1.setUserId(userId);
        friend1.setFriendId(friendId);
        friend1.setCreateTime(now);
        friendRepository.save(friend1);

        // 好友 -> 用户（双向关系）
        Friend friend2 = new Friend();
        friend2.setUserId(friendId);
        friend2.setFriendId(userId);
        friend2.setCreateTime(now);
        friendRepository.save(friend2);
    }

    /**
     * 删除好友（双向关系）
     * @param userId 用户ID
     * @param friendId 好友ID
     */
    public void removeFriend(Long userId, Long friendId) {
        // 验证不能删除自己
        if (userId.equals(friendId)) {
            throw new RuntimeException("无效操作");
        }

        // 检查是否是好友关系
        if (!friendRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new RuntimeException("不是好友关系");
        }

        // 删除双向好友关系
        friendRepository.deleteFriendship(userId, friendId);
    }

    /**
     * 获取用户的好友列表（包含备注信息）
     * @param userId 用户ID
     * @return 好友关系列表（包含用户信息和备注）
     */
    public List<Friend> getFriendList(Long userId) {
        return friendRepository.findByUserId(userId);
    }

    /**
     * 设置好友备注
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param remark 备注名称
     */
    public void setFriendRemark(Long userId, Long friendId, String remark) {
        // 验证不能设置自己
        if (userId.equals(friendId)) {
            throw new RuntimeException("无效操作");
        }

        // 检查是否是好友关系
        if (!friendRepository.existsByUserIdAndFriendId(userId, friendId)) {
            throw new RuntimeException("不是好友关系");
        }

        // 更新备注（如果remark为null或空字符串，则清空备注）
        String finalRemark = (remark != null && !remark.trim().isEmpty()) ? remark.trim() : null;
        friendRepository.updateRemark(userId, friendId, finalRemark);
    }

    /**
     * 检查两个用户是否是好友
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 是否是好友
     */
    public boolean isFriend(Long userId, Long friendId) {
        return friendRepository.existsByUserIdAndFriendId(userId, friendId);
    }

    /**
     * 获取好友数量
     * @param userId 用户ID
     * @return 好友数量
     */
    public int getFriendCount(Long userId) {
        return friendRepository.findByUserId(userId).size();
    }
}

