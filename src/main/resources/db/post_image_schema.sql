-- Post表结构更新（支持多图片功能）
-- 如果表已存在，需要先删除旧表（注意：会删除所有数据）
-- DROP TABLE IF EXISTS `post_image`;
-- DROP TABLE IF EXISTS `post`;

-- 1. 修改后的动态表（移除单图片字段，支持多图片）
CREATE TABLE IF NOT EXISTS `post` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '动态唯一ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '发布者ID',
    `content` TEXT NOT NULL COMMENT '动态文本内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户动态表（支持多图片）';

-- 2. 动态图片表（一个动态可以有多张图片）
CREATE TABLE IF NOT EXISTS `post_image` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片记录唯一ID',
    `post_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的动态ID',
    `image_path` VARCHAR(500) NOT NULL COMMENT '图片相对路径（本地路径）',
    `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '图片排序（数字越小越靠前）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_sort_order` (`post_id`, `sort_order`),
    CONSTRAINT `fk_image_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态图片表';

