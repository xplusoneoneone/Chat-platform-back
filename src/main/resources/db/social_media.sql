/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80038
 Source Host           : localhost:3306
 Source Schema         : social_media

 Target Server Type    : MySQL
 Target Server Version : 80038
 File Encoding         : 65001

 Date: 01/12/2025 20:26:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论唯一ID',
  `post_id` bigint UNSIGNED NOT NULL COMMENT '关联的动态ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '评论者ID',
  `parent_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '父评论ID（NULL表示顶级评论，有值表示回复某条评论）',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论内容',
  `like_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态评论表（支持回复）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for comment_like
-- ----------------------------
DROP TABLE IF EXISTS `comment_like`;
CREATE TABLE `comment_like`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '点赞记录唯一ID',
  `comment_id` bigint UNSIGNED NOT NULL COMMENT '被点赞的评论ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_comment_user`(`comment_id`, `user_id`) USING BTREE,
  INDEX `idx_comment_id`(`comment_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_like_comment` FOREIGN KEY (`comment_id`) REFERENCES `comment` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for friend
-- ----------------------------
DROP TABLE IF EXISTS `friend`;
CREATE TABLE `friend`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '好友关系唯一ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `friend_id` bigint UNSIGNED NOT NULL COMMENT '好友ID',
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '好友备注（用户给好友设置的备注名称）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '成为好友时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_friend`(`user_id`, `friend_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_friend_id`(`friend_id`) USING BTREE,
  CONSTRAINT `fk_friend_friend` FOREIGN KEY (`friend_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_friend_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '好友关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post
-- ----------------------------
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '动态唯一ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '发布者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '动态文本内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `like` int NOT NULL COMMENT '点赞',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户动态表（支持多图片）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for post_image
-- ----------------------------
DROP TABLE IF EXISTS `post_image`;
CREATE TABLE `post_image`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片记录唯一ID',
  `post_id` bigint UNSIGNED NOT NULL COMMENT '关联的动态ID',
  `image_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片相对路径（本地路径）',
  `image_data` longblob NULL COMMENT '图片二进制数据',
  `sort_order` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '图片排序（数字越小越靠前）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE,
  INDEX `idx_sort_order`(`post_id`, `sort_order`) USING BTREE,
  CONSTRAINT `fk_image_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '动态图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名（登录/显示用）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密存储）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'https://xxx.com/default-avatar.png' COMMENT '头像URL（默认占位图）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '用户创建时间',
  `sex` char(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `signature` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个性签名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE COMMENT '用户名唯一索引'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
