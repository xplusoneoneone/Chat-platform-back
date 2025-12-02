# Post表结构迁移指南（支持多图片）

## 表结构变更说明

### 1. post表修改
- **移除字段**：`image_url` VARCHAR(255) - 原来的单图片URL字段
- **保留字段**：
  - `id` - 动态唯一ID
  - `user_id` - 发布者ID
  - `content` - 动态文本内容
  - `create_time` - 发布时间

### 2. 新增post_image表
用于存储一个动态的多张图片，支持：
- 一个post可以有多张图片
- 图片路径保存为本地相对路径（如：`uploads/posts/2024/01/image_123456.jpg`）
- 支持图片排序（通过`sort_order`字段）

## 迁移步骤

### 方案一：如果post表已存在且有数据（推荐）

```sql
-- 1. 创建动态图片表
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

-- 2. 迁移现有数据（如果有image_url数据，需要迁移到post_image表）
-- 注意：需要根据实际情况调整迁移逻辑
INSERT INTO `post_image` (`post_id`, `image_path`, `sort_order`)
SELECT `id`, `image_url`, 0
FROM `post`
WHERE `image_url` IS NOT NULL AND `image_url` != '';

-- 3. 删除post表的image_url字段（可选，建议先备份数据）
-- ALTER TABLE `post` DROP COLUMN `image_url`;
```

### 方案二：如果表不存在或可以清空数据

直接执行 `post_image_schema.sql` 文件中的完整SQL语句。

## 功能说明

### 多图片支持
- 一个post可以关联多张图片
- 通过`post_image`表的一对多关系实现
- 图片路径使用本地相对路径，例如：
  - `uploads/posts/2024/01/image_123456.jpg`
  - `uploads/posts/2024/01/image_123457.jpg`

### 图片排序
- `sort_order`字段用于控制图片显示顺序
- 数字越小越靠前（0, 1, 2, ...）
- 创建时默认值为0

### 级联删除
- 当post被删除时，关联的所有图片记录会自动删除（CASCADE）
- 便于数据一致性维护

## 图片路径规范建议

建议使用以下路径格式：
```
uploads/posts/{year}/{month}/{filename}
```

例如：
- `uploads/posts/2024/01/post_123_image_001.jpg`
- `uploads/posts/2024/01/post_123_image_002.jpg`

这样可以：
1. 按年月组织文件，便于管理
2. 避免单目录文件过多
3. 相对路径便于本地开发和部署

## 注意事项

1. **执行迁移前请备份数据库**
2. 如果已有`image_url`数据，需要先迁移到`post_image`表
3. 删除`image_url`字段前，确保数据已迁移完成
4. 图片文件需要存储在应用可访问的目录下
5. 建议在`application.properties`中配置图片存储路径：
   ```properties
   # 图片上传配置
   upload.path=uploads
   upload.post.path=uploads/posts
   ```

## 查询示例

### 查询post及其所有图片
```sql
SELECT 
    p.id,
    p.user_id,
    p.content,
    p.create_time,
    pi.id as image_id,
    pi.image_path,
    pi.sort_order
FROM post p
LEFT JOIN post_image pi ON p.id = pi.post_id
WHERE p.id = ?
ORDER BY pi.sort_order ASC;
```

### 查询post的图片列表（按排序）
```sql
SELECT image_path
FROM post_image
WHERE post_id = ?
ORDER BY sort_order ASC;
```

