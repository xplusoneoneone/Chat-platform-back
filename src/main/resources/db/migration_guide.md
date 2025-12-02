# 评论表结构迁移指南

## 表结构变更说明

### 1. comment表新增字段
- `parent_id` BIGINT UNSIGNED DEFAULT NULL - 父评论ID，支持评论回复功能
  - NULL：表示顶级评论（直接评论动态）
  - 有值：表示回复某条评论
- `like_count` INT UNSIGNED NOT NULL DEFAULT 0 - 点赞数统计

### 2. 新增comment_like表
用于记录用户对评论的点赞关系，支持点赞/取消点赞功能。

## 迁移步骤

### 方案一：如果表已存在且有数据（推荐）

```sql
-- 1. 添加parent_id字段
ALTER TABLE `comment` 
ADD COLUMN `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID（NULL表示顶级评论，有值表示回复某条评论）' AFTER `user_id`,
ADD KEY `idx_parent_id` (`parent_id`),
ADD CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE;

-- 2. 添加like_count字段
ALTER TABLE `comment` 
ADD COLUMN `like_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数' AFTER `content`;

-- 3. 创建评论点赞表
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
```

### 方案二：如果表不存在或可以清空数据

直接执行 `schema_update.sql` 文件中的完整SQL语句。

## 功能说明

### 回复评论功能
- 顶级评论：`parent_id` 为 NULL，直接评论动态
- 回复评论：`parent_id` 指向被回复的评论ID
- 支持多级回复（回复的回复）

### 点赞功能
- 用户可以对任意评论点赞
- 同一用户对同一评论只能点赞一次（通过唯一索引保证）
- `comment.like_count` 字段记录点赞总数
- `comment_like` 表记录详细的点赞关系，便于查询和取消点赞

## 注意事项

1. 执行迁移前请备份数据库
2. 如果已有数据，建议在业务低峰期执行
3. 添加外键约束时，确保现有数据符合约束条件
4. `like_count` 字段初始值为0，后续需要通过应用层逻辑维护

