-- 将post_image表的image_path字段改为image_data BLOB字段（存储二进制图片数据）
-- 如果表已存在，需要先备份数据

-- 方案一：如果表已存在且有数据（需要先备份）
-- 1. 添加新字段
ALTER TABLE `post_image` 
ADD COLUMN `image_data` LONGBLOB COMMENT '图片二进制数据' AFTER `image_path`;

-- 2. 迁移数据（如果有image_path数据需要迁移，这里假设需要从文件系统读取）
-- 注意：如果之前存储的是路径，需要从文件系统读取文件内容并写入image_data
-- 这个操作需要在应用层完成，SQL无法直接读取文件系统

-- 3. 删除旧字段（迁移完成后）
-- ALTER TABLE `post_image` DROP COLUMN `image_path`;

-- 方案二：如果表不存在或可以清空数据，直接创建新表结构
-- DROP TABLE IF EXISTS `post_image`;
-- CREATE TABLE IF NOT EXISTS `post_image` (
--     `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片记录唯一ID',
--     `post_id` BIGINT UNSIGNED NOT NULL COMMENT '关联的动态ID',
--     `image_data` LONGBLOB NOT NULL COMMENT '图片二进制数据',
--     `sort_order` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '图片排序（数字越小越靠前）',
--     `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
--     PRIMARY KEY (`id`),
--     KEY `idx_post_id` (`post_id`),
--     KEY `idx_sort_order` (`post_id`, `sort_order`),
--     CONSTRAINT `fk_image_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='动态图片表（二进制存储）';

