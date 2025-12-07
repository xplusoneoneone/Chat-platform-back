package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Friend;
import com.example.demo.entity.FriendRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友控制器
 */
@RestController
@RequestMapping("/api/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 添加好友接口（发送好友申请）
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 操作结果
     */
    @PostMapping("/add")
    public ApiResponse<Map<String, Object>> addFriend(
            @RequestParam("userId") Long userId,
            @RequestParam("friendId") Long friendId) {
        try {
            FriendRequest request = friendService.addFriend(userId, friendId);
            
            // 查询申请者信息
            User requester = userRepository.findById(request.getRequesterId()).orElse(null);
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("id", request.getId());
            requestData.put("requesterId", request.getRequesterId());
            requestData.put("receiverId", request.getReceiverId());
            requestData.put("status", request.getStatus());
            requestData.put("createTime", request.getCreateTime());
            
            if (requester != null) {
                Map<String, Object> requesterData = new HashMap<>();
                requesterData.put("id", requester.getId());
                requesterData.put("username", requester.getUsername());
                requesterData.put("avatar", requester.getAvatar());
                requestData.put("requester", requesterData);
            }
            
            return ApiResponse.success("好友申请已发送，等待对方同意", requestData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("添加好友失败：" + e.getMessage());
        }
    }

    /**
     * 删除好友接口
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 操作结果
     */
    @PostMapping("/remove")
    public ApiResponse<Object> removeFriend(
            @RequestParam("userId") Long userId,
            @RequestParam("friendId") Long friendId) {
        try {
            friendService.removeFriend(userId, friendId);
            return ApiResponse.success("删除好友成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("删除好友失败：" + e.getMessage());
        }
    }

    /**
     * 获取好友列表接口（包含备注信息）
     * @param userId 用户ID
     * @return 好友列表
     */
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> getFriendList(@RequestParam("userId") Long userId) {
        try {
            List<Friend> friendRelations = friendService.getFriendList(userId);
            
            // 构建返回数据（包含备注信息）
            List<Map<String, Object>> friendList = new ArrayList<>();
            for (Friend friendRelation : friendRelations) {
                // 查询好友用户信息
                User friend = userRepository.findById(friendRelation.getFriendId())
                        .orElse(null);
                
                if (friend != null) {
                    Map<String, Object> friendData = new HashMap<>();
                    friendData.put("id", friend.getId());
                    friendData.put("username", friend.getUsername());
                    friendData.put("avatar", friend.getAvatar());
                    friendData.put("sex", friend.getSex());
                    friendData.put("location", friend.getLocation());
                    friendData.put("signature", friend.getSignature());
                    friendData.put("isLogging", friend.getIsLogging() != null ? friend.getIsLogging() : 0); // 在线状态
                    friendData.put("remark", friendRelation.getRemark()); // 备注信息
                    friendData.put("createTime", friendRelation.getCreateTime());
                    friendList.add(friendData);
                }
            }
            
            return ApiResponse.success("获取好友列表成功", friendList);
        } catch (Exception e) {
            return ApiResponse.error("获取好友列表失败：" + e.getMessage());
        }
    }

    /**
     * 检查是否是好友接口
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 是否是好友
     */
    @GetMapping("/check")
    public ApiResponse<Map<String, Object>> checkFriend(
            @RequestParam("userId") Long userId,
            @RequestParam("friendId") Long friendId) {
        try {
            boolean isFriend = friendService.isFriend(userId, friendId);
            Map<String, Object> result = new HashMap<>();
            result.put("isFriend", isFriend);
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取好友数量接口
     * @param userId 用户ID
     * @return 好友数量
     */
    @GetMapping("/count")
    public ApiResponse<Map<String, Object>> getFriendCount(@RequestParam("userId") Long userId) {
        try {
            int count = friendService.getFriendCount(userId);
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return ApiResponse.success("查询成功", result);
        } catch (Exception e) {
            return ApiResponse.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 设置好友备注接口
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param remark 备注名称（可选，传空字符串或null则清空备注）
     * @return 操作结果
     */
    @PostMapping("/set-remark")
    public ApiResponse<Object> setFriendRemark(
            @RequestParam("userId") Long userId,
            @RequestParam("friendId") Long friendId,
            @RequestParam(value = "remark", required = false) String remark) {
        try {
            friendService.setFriendRemark(userId, friendId, remark);
            return ApiResponse.success("设置备注成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("设置备注失败：" + e.getMessage());
        }
    }

    /**
     * 发送好友申请接口
     * @param requesterId 申请者ID
     * @param receiverId 接收者ID
     * @return 操作结果
     */
    @PostMapping("/request")
    public ApiResponse<Map<String, Object>> sendFriendRequest(
            @RequestParam("requesterId") Long requesterId,
            @RequestParam("receiverId") Long receiverId) {
        try {
            FriendRequest request = friendService.sendFriendRequest(requesterId, receiverId);
            
            // 查询申请者信息
            User requester = userRepository.findById(request.getRequesterId()).orElse(null);
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("id", request.getId());
            requestData.put("requesterId", request.getRequesterId());
            requestData.put("receiverId", request.getReceiverId());
            requestData.put("status", request.getStatus());
            requestData.put("createTime", request.getCreateTime());
            
            if (requester != null) {
                Map<String, Object> requesterData = new HashMap<>();
                requesterData.put("id", requester.getId());
                requesterData.put("username", requester.getUsername());
                requesterData.put("avatar", requester.getAvatar());
                requestData.put("requester", requesterData);
            }
            
            return ApiResponse.success("发送好友申请成功", requestData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("发送好友申请失败：" + e.getMessage());
        }
    }

    /**
     * 获取待处理的好友申请列表接口
     * @param receiverId 接收者ID（当前用户）
     * @return 好友申请列表
     */
    @GetMapping("/requests")
    public ApiResponse<List<Map<String, Object>>> getFriendRequests(@RequestParam("receiverId") Long receiverId) {
        try {
            List<FriendRequest> requests = friendService.getPendingFriendRequests(receiverId);
            
            // 构建返回数据（包含申请者信息）
            List<Map<String, Object>> requestList = new ArrayList<>();
            for (FriendRequest request : requests) {
                // 查询申请者信息
                User requester = userRepository.findById(request.getRequesterId()).orElse(null);
                
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("id", request.getId());
                requestData.put("requesterId", request.getRequesterId());
                requestData.put("receiverId", request.getReceiverId());
                requestData.put("status", request.getStatus());
                requestData.put("createTime", request.getCreateTime());
                requestData.put("updateTime", request.getUpdateTime());
                
                // 添加申请者信息
                if (requester != null) {
                    Map<String, Object> requesterData = new HashMap<>();
                    requesterData.put("id", requester.getId());
                    requesterData.put("username", requester.getUsername());
                    requesterData.put("avatar", requester.getAvatar());
                    requesterData.put("sex", requester.getSex());
                    requesterData.put("signature", requester.getSignature());
                    requestData.put("requester", requesterData);
                }
                
                requestList.add(requestData);
            }
            
            return ApiResponse.success("获取好友申请列表成功", requestList);
        } catch (Exception e) {
            return ApiResponse.error("获取好友申请列表失败：" + e.getMessage());
        }
    }

    /**
     * 同意好友申请接口
     * @param requestId 申请ID
     * @param receiverId 接收者ID（当前用户）
     * @return 操作结果
     */
    @PostMapping("/request/{requestId}/accept")
    public ApiResponse<Map<String, Object>> acceptFriendRequest(
            @PathVariable("requestId") Long requestId,
            @RequestParam("receiverId") Long receiverId) {
        try {
            Friend friend = friendService.acceptFriendRequest(requestId, receiverId);
            
            // 查询好友信息
            User friendUser = userRepository.findById(friend.getFriendId()).orElse(null);
            
            Map<String, Object> friendData = new HashMap<>();
            friendData.put("id", friend.getId());
            friendData.put("userId", friend.getUserId());
            friendData.put("friendId", friend.getFriendId());
            friendData.put("remark", friend.getRemark());
            friendData.put("createTime", friend.getCreateTime());
            
            if (friendUser != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", friendUser.getId());
                userData.put("username", friendUser.getUsername());
                userData.put("avatar", friendUser.getAvatar());
                userData.put("sex", friendUser.getSex());
                userData.put("signature", friendUser.getSignature());
                friendData.put("friend", userData);
            }
            
            return ApiResponse.success("同意好友申请成功", friendData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("同意好友申请失败：" + e.getMessage());
        }
    }

    /**
     * 拒绝好友申请接口
     * @param requestId 申请ID
     * @param receiverId 接收者ID（当前用户）
     * @return 操作结果
     */
    @PostMapping("/request/{requestId}/reject")
    public ApiResponse<Object> rejectFriendRequest(
            @PathVariable("requestId") Long requestId,
            @RequestParam("receiverId") Long receiverId) {
        try {
            friendService.rejectFriendRequest(requestId, receiverId);
            return ApiResponse.success("拒绝好友申请成功", null);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("拒绝好友申请失败：" + e.getMessage());
        }
    }
}

