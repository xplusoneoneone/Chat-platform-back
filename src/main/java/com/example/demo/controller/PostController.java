package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PageRequest;
import com.example.demo.dto.PageResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostImage;
import com.example.demo.entity.User;
import com.example.demo.repository.PostImageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态控制器
 */
@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 发布动态接口
     * @param userId 用户ID
     * @param content 动态内容
     * @param imagePaths 图片路径列表（可选，本地相对路径）
     * @return 发布结果
     */
    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createPost(
            @RequestParam("userId") Long userId,
            @RequestParam("content") String content,
            @RequestParam(value = "imagePaths", required = false) List<String> imagePaths) {
        try {
            // 验证内容不能为空
            if (content == null || content.trim().isEmpty()) {
                return ApiResponse.error(400, "动态内容不能为空");
            }

            // 调用服务层创建动态
            Post post = postService.createPost(userId, content, imagePaths);

            // 查询动态的图片列表
            List<PostImage> postImages = postImageRepository.findByPostId(post.getId());
            List<String> savedImagePaths = new ArrayList<>();
            for (PostImage postImage : postImages) {
                savedImagePaths.add(postImage.getImagePath());
            }

            // 构建返回数据
            Map<String, Object> postData = new HashMap<>();
            postData.put("id", post.getId());
            postData.put("userId", post.getUserId());
            postData.put("content", post.getContent());
            postData.put("like", post.getLike());
            postData.put("images", savedImagePaths);
            postData.put("createTime", post.getCreateTime());

            return ApiResponse.success("发布成功", postData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("发布失败：" + e.getMessage());
        }
    }

    /**
     * 获取动态列表接口（分页，好友优先）
     * @param userId 当前用户ID
     * @param page 页码（从1开始，默认1）
     * @param size 每页大小（默认10）
     * @return 动态列表（好友动态优先，按时间倒序）
     */
    @GetMapping("/list")
    public ApiResponse<Map<String, Object>> getPostList(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        try {
            PageRequest pageRequest = new PageRequest(page, size);
            PageResponse<Post> pageResponse = postService.getPostList(userId, pageRequest);

            // 构建返回数据（包含用户信息和图片）
            List<Map<String, Object>> postList = new ArrayList<>();
            for (Post post : pageResponse.getContent()) {
                // 查询用户信息
                User user = userRepository.findById(post.getUserId()).orElse(null);
                
                // 查询图片列表
                List<PostImage> postImages = postImageRepository.findByPostId(post.getId());
                List<String> imagePaths = new ArrayList<>();
                for (PostImage postImage : postImages) {
                    imagePaths.add(postImage.getImagePath());
                }

                Map<String, Object> postData = new HashMap<>();
                postData.put("id", post.getId());
                postData.put("userId", post.getUserId());
                postData.put("content", post.getContent());
                postData.put("like", post.getLike());
                postData.put("images", imagePaths);
                postData.put("createTime", post.getCreateTime());
                
                // 添加用户信息
                if (user != null) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("username", user.getUsername());
                    userData.put("avatar", user.getAvatar());
                    userData.put("sex", user.getSex());
                    userData.put("signature", user.getSignature());
                    postData.put("user", userData);
                }
                
                postList.add(postData);
            }

            // 构建分页信息
            Map<String, Object> result = new HashMap<>();
            result.put("content", postList);
            result.put("page", pageResponse.getPage());
            result.put("size", pageResponse.getSize());
            result.put("total", pageResponse.getTotal());
            result.put("totalPages", pageResponse.getTotalPages());

            return ApiResponse.success("获取动态列表成功", result);
        } catch (Exception e) {
            return ApiResponse.error("获取动态列表失败：" + e.getMessage());
        }
    }
}

