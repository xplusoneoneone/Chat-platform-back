-- 好友表添加备注字段
-- 为friend表添加remark字段，用于存储用户给好友设置的备注名称

ALTER TABLE `friend` 
ADD COLUMN `remark` VARCHAR(50) DEFAULT NULL COMMENT '好友备注（用户给好友设置的备注名称）' AFTER `friend_id`;

