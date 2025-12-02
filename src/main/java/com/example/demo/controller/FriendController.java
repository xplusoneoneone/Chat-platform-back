package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.Friend;
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
     * 添加好友接口
     * @param userId 用户ID
     * @param friendId 好友ID
     * @return 操作结果
     */
    @PostMapping("/add")
    public ApiResponse<Object> addFriend(
            @RequestParam("userId") Long userId,
            @RequestParam("friendId") Long friendId) {
        try {
            friendService.addFriend(userId, friendId);
            return ApiResponse.success("添加好友成功", null);
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
                    friendData.put("signature", friend.getSignature());
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
}

