package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论服务层
 */
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    /**
     * 创建评论
     * @param postId 帖子ID
     * @param userId 用户ID
     * @param parentId 父评论ID（可选，null表示顶级评论）
     * @param content 评论内容
     * @return 创建成功的评论
     */
    public Comment createComment(Long postId, Long userId, Long parentId, String content) {
        // 验证帖子是否存在
        if (postRepository.findById(postId).isEmpty()) {
            throw new RuntimeException("帖子不存在");
        }

        // 如果parentId不为null，验证父评论是否存在
        if (parentId != null) {
            if (commentRepository.findById(parentId).isEmpty()) {
                throw new RuntimeException("父评论不存在");
            }
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(content);
        comment.setLikeCount(0); // 默认点赞数为0
        comment.setCreateTime(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    /**
     * 根据ID查找评论
     */
    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
    }

    /**
     * 根据帖子ID查找所有评论
     */
    public List<Comment> findByPostId(Long postId) {
        return commentRepository.findByPostId(postId);
    }

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 更新后的评论
     */
    public Comment likeComment(Long commentId, Long userId) {
        // 验证评论是否存在
        if (commentRepository.findById(commentId).isEmpty()) {
            throw new RuntimeException("评论不存在");
        }

        // 检查是否已经点赞过
        if (commentRepository.existsLike(commentId, userId)) {
            throw new RuntimeException("您已经点赞过这条评论");
        }

        // 添加点赞记录
        commentRepository.addLike(commentId, userId);

        // 更新评论点赞数
        commentRepository.incrementLikeCount(commentId);

        // 返回更新后的评论
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
    }

    /**
     * 取消点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 更新后的评论
     */
    public Comment unlikeComment(Long commentId, Long userId) {
        // 验证评论是否存在
        if (commentRepository.findById(commentId).isEmpty()) {
            throw new RuntimeException("评论不存在");
        }

        // 检查是否已经点赞过
        if (!commentRepository.existsLike(commentId, userId)) {
            throw new RuntimeException("您还没有点赞过这条评论");
        }

        // 删除点赞记录
        commentRepository.removeLike(commentId, userId);

        // 更新评论点赞数
        commentRepository.decrementLikeCount(commentId);

        // 返回更新后的评论
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在"));
    }
}

