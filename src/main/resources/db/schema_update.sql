-- 评论表结构更新（支持回复和点赞功能）
-- 如果表已存在，需要先删除旧表（注意：会删除所有数据）
-- DROP TABLE IF EXISTS `comment_like`;
-- DROP TABLE IF EXISTS `comment`;

-- 1. 修改后的评论表（支持回复功能）
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论唯一ID',
    `post_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的动态ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '评论者ID',
    `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID（NULL表示顶级评论，有值表示回复某条评论）',
    `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
    `like_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`),
    CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态评论表（支持回复）';

-- 2. 评论点赞表（记录用户对评论的点赞关系）
CREATE TABLE IF NOT EXISTS `comment_like` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞记录唯一ID',
    `comment_id` BIGINT UNSIGNED NOT NULL COMMENT '被点赞的评论ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_comment_id` (`comment_id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论点赞表';

