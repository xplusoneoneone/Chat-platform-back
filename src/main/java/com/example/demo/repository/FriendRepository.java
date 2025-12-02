package com.example.demo.repository;

import com.example.demo.entity.Friend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 好友关系数据访问层
 */
@Repository
public class FriendRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 保存好友关系
     */
    public Friend save(Friend friend) {
        String sql = "INSERT INTO friend (user_id, friend_id, remark, create_time) VALUES (?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, friend.getUserId());
            ps.setLong(2, friend.getFriendId());
            ps.setString(3, friend.getRemark());
            ps.setObject(4, friend.getCreateTime() != null ? friend.getCreateTime() : LocalDateTime.now());
            return ps;
        }, keyHolder);
        
        Long id = keyHolder.getKey().longValue();
        friend.setId(id);
        return friend;
    }

    /**
     * 检查好友关系是否存在
     */
    public boolean existsByUserIdAndFriendId(Long userId, Long friendId) {
        String sql = "SELECT COUNT(*) FROM friend WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    /**
     * 根据用户ID和好友ID查找好友关系
     */
    public Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId) {
        String sql = "SELECT id, user_id as userId, friend_id as friendId, remark, create_time as createTime " +
                     "FROM friend WHERE user_id = ? AND friend_id = ?";
        try {
            Friend friend = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(Friend.class), userId, friendId);
            return Optional.ofNullable(friend);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据用户ID查找所有好友ID列表
     */
    public List<Long> findFriendIdsByUserId(Long userId) {
        String sql = "SELECT friend_id FROM friend WHERE user_id = ? ORDER BY create_time DESC";
        return jdbcTemplate.queryForList(sql, Long.class, userId);
    }

    /**
     * 根据用户ID查找所有好友关系
     */
    public List<Friend> findByUserId(Long userId) {
        String sql = "SELECT id, user_id as userId, friend_id as friendId, remark, create_time as createTime " +
                     "FROM friend WHERE user_id = ? ORDER BY create_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Friend.class), userId);
    }

    /**
     * 更新好友备注
     */
    public void updateRemark(Long userId, Long friendId, String remark) {
        String sql = "UPDATE friend SET remark = ? WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, remark, userId, friendId);
    }

    /**
     * 删除好友关系
     */
    public void deleteByUserIdAndFriendId(Long userId, Long friendId) {
        String sql = "DELETE FROM friend WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    /**
     * 删除双向好友关系（删除A和B之间的所有关系）
     */
    public void deleteFriendship(Long userId, Long friendId) {
        // 删除 user_id -> friend_id 的关系
        deleteByUserIdAndFriendId(userId, friendId);
        // 删除 friend_id -> user_id 的关系（如果存在）
        deleteByUserIdAndFriendId(friendId, userId);
    }
}

