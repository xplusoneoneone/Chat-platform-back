package com.example.demo.service;

import com.example.demo.entity.Friend;
import com.example.demo.entity.FriendRequest;
import com.example.demo.repository.FriendRepository;
import com.example.demo.repository.FriendRequestRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 好友服务层
 */
@Service
public class FriendService {

    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    /**
     * 添加好友（发送好友申请）
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 好友申请
     */
    public FriendRequest addFriend(Long userId, Long friendId) {
        // 调用发送好友申请的方法
        return sendFriendRequest(userId, friendId);
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

    /**
     * 发送好友申请
     * @param requesterId 申请者ID
     * @param receiverId 接收者ID
     * @return 好友申请
     */
    public FriendRequest sendFriendRequest(Long requesterId, Long receiverId) {
        // 验证不能添加自己为好友
        if (requesterId.equals(receiverId)) {
            throw new RuntimeException("不能向自己发送好友申请");
        }

        // 验证用户是否存在
        if (userRepository.findById(requesterId).isEmpty()) {
            throw new RuntimeException("申请者不存在");
        }

        // 验证接收者是否存在
        if (userRepository.findById(receiverId).isEmpty()) {
            throw new RuntimeException("接收者不存在");
        }

        // 检查是否已经是好友
        if (friendRepository.existsByUserIdAndFriendId(requesterId, receiverId)) {
            throw new RuntimeException("已经是好友关系");
        }

        // 检查是否已有待处理的申请
        if (friendRequestRepository.existsPendingRequest(requesterId, receiverId)) {
            throw new RuntimeException("已发送好友申请，请等待对方处理");
        }

        // 检查对方是否已向你发送申请（如果已发送，则直接同意）
        Optional<FriendRequest> reverseRequest = friendRequestRepository.findPendingByRequesterAndReceiver(receiverId, requesterId);
        if (reverseRequest.isPresent()) {
            // 直接同意对方的申请，并创建双向好友关系
            acceptFriendRequest(reverseRequest.get().getId(), requesterId);
            // 然后创建自己的申请并标记为已同意（或者不创建，因为已经是好友了）
            // 这里我们选择不创建新的申请，因为已经是好友了
            throw new RuntimeException("对方已向你发送好友申请，已自动同意并成为好友");
        }

        // 创建好友申请
        FriendRequest request = new FriendRequest();
        request.setRequesterId(requesterId);
        request.setReceiverId(receiverId);
        request.setStatus("pending");
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());

        return friendRequestRepository.save(request);
    }

    /**
     * 获取待处理的好友申请列表
     * @param receiverId 接收者ID（当前用户）
     * @return 好友申请列表
     */
    public List<FriendRequest> getPendingFriendRequests(Long receiverId) {
        return friendRequestRepository.findPendingByReceiverId(receiverId);
    }

    /**
     * 同意好友申请
     * @param requestId 申请ID
     * @param receiverId 接收者ID（当前用户，用于验证权限）
     * @return 好友关系
     */
    public Friend acceptFriendRequest(Long requestId, Long receiverId) {
        // 查找申请
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("好友申请不存在"));

        // 验证权限：只有接收者才能同意申请
        if (!request.getReceiverId().equals(receiverId)) {
            throw new RuntimeException("无权处理此申请");
        }

        // 验证申请状态
        if (!"pending".equals(request.getStatus())) {
            throw new RuntimeException("该申请已处理");
        }

        // 检查是否已经是好友
        if (friendRepository.existsByUserIdAndFriendId(request.getRequesterId(), request.getReceiverId())) {
            // 如果已经是好友，直接更新申请状态为已同意
            friendRequestRepository.updateStatus(requestId, "accepted");
            throw new RuntimeException("已经是好友关系");
        }

        // 创建双向好友关系
        LocalDateTime now = LocalDateTime.now();

        // 申请者 -> 接收者
        Friend friend1 = new Friend();
        friend1.setUserId(request.getRequesterId());
        friend1.setFriendId(request.getReceiverId());
        friend1.setCreateTime(now);
        friendRepository.save(friend1);

        // 接收者 -> 申请者（双向关系）
        Friend friend2 = new Friend();
        friend2.setUserId(request.getReceiverId());
        friend2.setFriendId(request.getRequesterId());
        friend2.setCreateTime(now);
        Friend savedFriend = friendRepository.save(friend2);

        // 更新申请状态为已同意
        friendRequestRepository.updateStatus(requestId, "accepted");
        // 处理完成后删除申请记录
        friendRequestRepository.deleteById(requestId);

        return savedFriend;
    }

    /**
     * 拒绝好友申请
     * @param requestId 申请ID
     * @param receiverId 接收者ID（当前用户，用于验证权限）
     */
    public void rejectFriendRequest(Long requestId, Long receiverId) {
        // 查找申请
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("好友申请不存在"));

        // 验证权限：只有接收者才能拒绝申请
        if (!request.getReceiverId().equals(receiverId)) {
            throw new RuntimeException("无权处理此申请");
        }

        // 验证申请状态
        if (!"pending".equals(request.getStatus())) {
            throw new RuntimeException("该申请已处理");
        }

        // 更新申请状态为已拒绝
        friendRequestRepository.updateStatus(requestId, "rejected");
        // 处理完成后删除申请记录
        friendRequestRepository.deleteById(requestId);
    }
}

