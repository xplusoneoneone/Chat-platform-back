package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.PageRequest;
import com.example.demo.dto.PageResponse;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostImage;
import com.example.demo.entity.User;
import com.example.demo.repository.PostImageRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CommentService;
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

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentService commentService;

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
            postData.put("isLike", false); // 新发布的帖子默认未点赞
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

            // 批量查询当前用户已点赞的帖子ID集合（优化性能）
            List<Long> postIds = new ArrayList<>();
            for (Post post : pageResponse.getContent()) {
                postIds.add(post.getId());
            }
            java.util.Set<Long> likedPostIds = postRepository.findLikedPostIds(postIds, userId);

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
                postData.put("isLike", likedPostIds.contains(post.getId())); // 添加是否点赞字段
                postData.put("images", imagePaths);
                postData.put("createTime", post.getCreateTime());
                
                // 添加用户信息
                if (user != null) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("username", user.getUsername());
                    userData.put("avatar", user.getAvatar());
                    userData.put("sex", user.getSex());
                    userData.put("location", user.getLocation());
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

    /**
     * 点赞帖子接口
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 点赞结果
     */
    @PostMapping("/{postId}/like")
    public ApiResponse<Map<String, Object>> likePost(
            @PathVariable("postId") Long postId,
            @RequestParam("userId") Long userId) {
        try {
            // 调用服务层点赞帖子
            Post post = postService.likePost(postId, userId);

            // 查询帖子作者信息
            User user = userRepository.findById(post.getUserId()).orElse(null);

            // 查询图片列表
            List<PostImage> postImages = postImageRepository.findByPostId(post.getId());
            List<String> imagePaths = new ArrayList<>();
            for (PostImage postImage : postImages) {
                imagePaths.add(postImage.getImagePath());
            }

            // 构建返回数据
            Map<String, Object> postData = new HashMap<>();
            postData.put("id", post.getId());
            postData.put("userId", post.getUserId());
            postData.put("content", post.getContent());
            postData.put("like", post.getLike());
            postData.put("isLike", true); // 点赞后设置为true
            postData.put("images", imagePaths);
            postData.put("createTime", post.getCreateTime());

            // 添加作者信息
            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("avatar", user.getAvatar());
                postData.put("user", userData);
            }

            return ApiResponse.success("点赞成功", postData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("点赞失败：" + e.getMessage());
        }
    }

    /**
     * 取消点赞帖子接口
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 取消点赞结果
     */
    @PostMapping("/{postId}/unlike")
    public ApiResponse<Map<String, Object>> unlikePost(
            @PathVariable("postId") Long postId,
            @RequestParam("userId") Long userId) {
        try {
            // 调用服务层取消点赞帖子
            Post post = postService.unlikePost(postId, userId);

            // 查询帖子作者信息
            User user = userRepository.findById(post.getUserId()).orElse(null);

            // 查询图片列表
            List<PostImage> postImages = postImageRepository.findByPostId(post.getId());
            List<String> imagePaths = new ArrayList<>();
            for (PostImage postImage : postImages) {
                imagePaths.add(postImage.getImagePath());
            }

            // 构建返回数据
            Map<String, Object> postData = new HashMap<>();
            postData.put("id", post.getId());
            postData.put("userId", post.getUserId());
            postData.put("content", post.getContent());
            postData.put("like", post.getLike());
            postData.put("isLike", false); // 取消点赞后设置为false
            postData.put("images", imagePaths);
            postData.put("createTime", post.getCreateTime());

            // 添加作者信息
            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("avatar", user.getAvatar());
                postData.put("user", userData);
            }

            return ApiResponse.success("取消点赞成功", postData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("取消点赞失败：" + e.getMessage());
        }
    }

    /**
     * 评论帖子接口
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param parentId 父评论ID（可选，不传或为null表示顶级评论，有值表示回复某条评论）
     * @param content 评论内容
     * @return 评论结果
     */
    @PostMapping("/comment")
    public ApiResponse<Map<String, Object>> createComment(
            @RequestParam("postId") Long postId,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestParam("content") String content) {
        try {
            // 验证内容不能为空
            if (content == null || content.trim().isEmpty()) {
                return ApiResponse.error(400, "评论内容不能为空");
            }

            // 调用服务层创建评论
            Comment comment = commentService.createComment(postId, userId, parentId, content);

            // 查询评论者信息
            User user = userRepository.findById(comment.getUserId()).orElse(null);

            // 构建返回数据
            Map<String, Object> commentData = new HashMap<>();
            commentData.put("id", comment.getId());
            commentData.put("postId", comment.getPostId());
            commentData.put("userId", comment.getUserId());
            commentData.put("parentId", comment.getParentId());
            commentData.put("content", comment.getContent());
            commentData.put("likeCount", comment.getLikeCount());
            commentData.put("createTime", comment.getCreateTime());

            // 添加评论者信息
            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("avatar", user.getAvatar());
                commentData.put("user", userData);
            }

            return ApiResponse.success("评论成功", commentData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("评论失败：" + e.getMessage());
        }
    }

    /**
     * 获取帖子评论列表接口
     * @param postId 帖子ID
     * @return 评论列表
     */
    @GetMapping("/{postId}/comments")
    public ApiResponse<List<Map<String, Object>>> getComments(@PathVariable("postId") Long postId) {
        try {
            // 调用服务层获取评论列表
            List<Comment> comments = commentService.findByPostId(postId);

            // 构建返回数据（包含用户信息）
            List<Map<String, Object>> commentList = new ArrayList<>();
            for (Comment comment : comments) {
                // 查询评论者信息
                User user = userRepository.findById(comment.getUserId()).orElse(null);

                Map<String, Object> commentData = new HashMap<>();
                commentData.put("id", comment.getId());
                commentData.put("postId", comment.getPostId());
                commentData.put("userId", comment.getUserId());
                commentData.put("parentId", comment.getParentId());
                commentData.put("content", comment.getContent());
                commentData.put("likeCount", comment.getLikeCount());
                commentData.put("createTime", comment.getCreateTime());

                // 添加评论者信息
                if (user != null) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("username", user.getUsername());
                    userData.put("avatar", user.getAvatar());
                    commentData.put("user", userData);
                }

                commentList.add(commentData);
            }

            return ApiResponse.success("获取评论列表成功", commentList);
        } catch (Exception e) {
            return ApiResponse.error("获取评论列表失败：" + e.getMessage());
        }
    }

    /**
     * 点赞评论接口
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 点赞结果
     */
    @PostMapping("/comment/{commentId}/like")
    public ApiResponse<Map<String, Object>> likeComment(
            @PathVariable("commentId") Long commentId,
            @RequestParam("userId") Long userId) {
        try {
            // 调用服务层点赞评论
            Comment comment = commentService.likeComment(commentId, userId);

            // 查询评论者信息
            User user = userRepository.findById(comment.getUserId()).orElse(null);

            // 构建返回数据
            Map<String, Object> commentData = new HashMap<>();
            commentData.put("id", comment.getId());
            commentData.put("postId", comment.getPostId());
            commentData.put("userId", comment.getUserId());
            commentData.put("parentId", comment.getParentId());
            commentData.put("content", comment.getContent());
            commentData.put("likeCount", comment.getLikeCount());
            commentData.put("createTime", comment.getCreateTime());

            // 添加评论者信息
            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("avatar", user.getAvatar());
                commentData.put("user", userData);
            }

            return ApiResponse.success("点赞成功", commentData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("点赞失败：" + e.getMessage());
        }
    }

    /**
     * 取消点赞评论接口
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 取消点赞结果
     */
    @PostMapping("/comment/{commentId}/unlike")
    public ApiResponse<Map<String, Object>> unlikeComment(
            @PathVariable("commentId") Long commentId,
            @RequestParam("userId") Long userId) {
        try {
            // 调用服务层取消点赞评论
            Comment comment = commentService.unlikeComment(commentId, userId);

            // 查询评论者信息
            User user = userRepository.findById(comment.getUserId()).orElse(null);

            // 构建返回数据
            Map<String, Object> commentData = new HashMap<>();
            commentData.put("id", comment.getId());
            commentData.put("postId", comment.getPostId());
            commentData.put("userId", comment.getUserId());
            commentData.put("parentId", comment.getParentId());
            commentData.put("content", comment.getContent());
            commentData.put("likeCount", comment.getLikeCount());
            commentData.put("createTime", comment.getCreateTime());

            // 添加评论者信息
            if (user != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("avatar", user.getAvatar());
                commentData.put("user", userData);
            }

            return ApiResponse.success("取消点赞成功", commentData);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("取消点赞失败：" + e.getMessage());
        }
    }
}

