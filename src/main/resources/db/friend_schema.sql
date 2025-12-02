-- 好友表结构（每个用户好友独立）
-- 如果表已存在，需要先删除旧表（注意：会删除所有数据）
-- DROP TABLE IF EXISTS `friend`;

-- 好友表（双向好友关系）
-- 注意：字段的COMMENT是支持的，但CONSTRAINT和KEY的COMMENT在MySQL中不支持
CREATE TABLE IF NOT EXISTS `friend` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '好友关系唯一ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `friend_id` BIGINT UNSIGNED NOT NULL COMMENT '好友ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成为好友时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_friend_id` (`friend_id`),
    CONSTRAINT `fk_friend_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_friend_friend` FOREIGN KEY (`friend_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

