package com.example.demo.service;

import com.example.demo.dto.PageRequest;
import com.example.demo.dto.PageResponse;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostImage;
import com.example.demo.repository.FriendRepository;
import com.example.demo.repository.PostImageRepository;
import com.example.demo.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态服务层
 */
@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private FriendRepository friendRepository;

    /**
     * 创建动态
     * @param userId 用户ID
     * @param content 动态内容
     * @param imagePaths 图片路径列表（本地相对路径）
     * @return 创建成功的动态
     */
    public Post createPost(Long userId, String content, List<String> imagePaths) {
        // 创建动态
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(content);
        post.setLike(0); // 默认点赞数为0
        post.setCreateTime(LocalDateTime.now());
        
        Post savedPost = postRepository.save(post);

        // 处理图片路径
        if (imagePaths != null && !imagePaths.isEmpty()) {
            List<PostImage> postImages = new ArrayList<>();
            int sortOrder = 0;

            for (String imagePath : imagePaths) {
                if (imagePath != null && !imagePath.trim().isEmpty()) {
                    // 创建图片记录
                    PostImage postImage = new PostImage();
                    postImage.setPostId(savedPost.getId());
                    postImage.setImagePath(imagePath.trim());
                    postImage.setSortOrder(sortOrder++);
                    postImage.setCreateTime(LocalDateTime.now());
                    
                    postImages.add(postImage);
                }
            }

            // 批量保存图片记录
            if (!postImages.isEmpty()) {
                postImageRepository.saveBatch(postImages);
            }
        }

        return savedPost;
    }

    /**
     * 根据ID查找动态
     */
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
    }

    /**
     * 根据用户ID查找动态列表
     */
    public List<Post> findByUserId(Long userId) {
        return postRepository.findByUserId(userId);
    }

    /**
     * 查找所有动态列表
     */
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    /**
     * 获取动态列表（分页，好友优先，包括自己的动态）
     * @param userId 当前用户ID
     * @param pageRequest 分页请求
     * @return 分页响应（好友动态和自己的动态在前，非好友动态在后，都按时间倒序）
     */
    public PageResponse<Post> getPostList(Long userId, PageRequest pageRequest) {
        // 获取用户的好友ID列表
        List<Long> friendIds = friendRepository.findFriendIdsByUserId(userId);
        
        // 将自己的ID也加入到好友列表中，这样自己的动态会和好友动态一起优先展示
        List<Long> friendIdsIncludingSelf = new ArrayList<>(friendIds);
        if (!friendIdsIncludingSelf.contains(userId)) {
            friendIdsIncludingSelf.add(userId);
        }

        List<Post> allPosts = new ArrayList<>();
        long total = 0;

        // 先获取好友和自己的动态（优先展示）
        int pageSize = pageRequest.getSize();
        int offset = pageRequest.getOffset();
        
        // 查询好友和自己的动态
        List<Post> friendAndSelfPosts = postRepository.findFriendPosts(
            userId, friendIdsIncludingSelf, 0, pageSize * 2);
        allPosts.addAll(friendAndSelfPosts);
        
        // 如果好友和自己的动态不够一页，再查询非好友动态
        if (friendAndSelfPosts.size() < pageSize) {
            int remainingSize = pageSize - friendAndSelfPosts.size();
            List<Post> nonFriendPosts = postRepository.findNonFriendPosts(
                userId, friendIdsIncludingSelf, 0, remainingSize);
            allPosts.addAll(nonFriendPosts);
        }
        
        // 统计总数（包括自己的动态）
        long friendAndSelfCount = postRepository.countFriendPosts(friendIdsIncludingSelf);
        long nonFriendCount = postRepository.countNonFriendPosts(userId, friendIdsIncludingSelf);
        total = friendAndSelfCount + nonFriendCount;

        // 按时间排序（最新的在前）
        allPosts.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        // 分页处理
        int start = offset;
        int end = Math.min(start + pageSize, allPosts.size());
        List<Post> pagedPosts = start < allPosts.size() 
            ? allPosts.subList(start, end) 
            : new ArrayList<>();

        return new PageResponse<>(pagedPosts, pageRequest.getPage(), pageRequest.getSize(), total);
    }

    /**
     * 点赞帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 更新后的帖子
     */
    public Post likePost(Long postId, Long userId) {
        // 验证帖子是否存在
        if (postRepository.findById(postId).isEmpty()) {
            throw new RuntimeException("帖子不存在");
        }

        // 检查是否已经点赞过
        if (postRepository.existsLike(postId, userId)) {
            throw new RuntimeException("您已经点赞过这条帖子");
        }

        // 添加点赞记录
        postRepository.addLike(postId, userId);

        // 更新帖子点赞数
        postRepository.incrementLikeCount(postId);

        // 返回更新后的帖子
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
    }

    /**
     * 取消点赞帖子
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 更新后的帖子
     */
    public Post unlikePost(Long postId, Long userId) {
        // 验证帖子是否存在
        if (postRepository.findById(postId).isEmpty()) {
            throw new RuntimeException("帖子不存在");
        }

        // 检查是否已经点赞过
        if (!postRepository.existsLike(postId, userId)) {
            throw new RuntimeException("您还没有点赞过这条帖子");
        }

        // 删除点赞记录
        postRepository.removeLike(postId, userId);

        // 更新帖子点赞数
        postRepository.decrementLikeCount(postId);

        // 返回更新后的帖子
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
    }
}

