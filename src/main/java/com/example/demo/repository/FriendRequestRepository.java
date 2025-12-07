package com.example.demo.repository;

import com.example.demo.entity.FriendRequest;
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
 * 好友申请数据访问层
 */
@Repository
public class FriendRequestRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 保存好友申请
     */
    public FriendRequest save(FriendRequest friendRequest) {
        String sql = "INSERT INTO friend_request (requester_id, receiver_id, status, create_time, update_time) VALUES (?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, friendRequest.getRequesterId());
            ps.setLong(2, friendRequest.getReceiverId());
            ps.setString(3, friendRequest.getStatus() != null ? friendRequest.getStatus() : "pending");
            ps.setObject(4, friendRequest.getCreateTime() != null ? friendRequest.getCreateTime() : LocalDateTime.now());
            ps.setObject(5, friendRequest.getUpdateTime() != null ? friendRequest.getUpdateTime() : LocalDateTime.now());
            return ps;
        }, keyHolder);
        
        Long id = keyHolder.getKey().longValue();
        friendRequest.setId(id);
        return friendRequest;
    }

    /**
     * 根据ID查找好友申请
     */
    public Optional<FriendRequest> findById(Long id) {
        String sql = "SELECT id, requester_id as requesterId, receiver_id as receiverId, status, create_time as createTime, update_time as updateTime " +
                     "FROM friend_request WHERE id = ?";
        try {
            FriendRequest request = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(FriendRequest.class), id);
            return Optional.ofNullable(request);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据接收者ID查找待处理的好友申请列表
     */
    public List<FriendRequest> findPendingByReceiverId(Long receiverId) {
        String sql = "SELECT id, requester_id as requesterId, receiver_id as receiverId, status, create_time as createTime, update_time as updateTime " +
                     "FROM friend_request WHERE receiver_id = ? AND status = 'pending' ORDER BY create_time DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(FriendRequest.class), receiverId);
    }

    /**
     * 根据申请者和接收者ID查找待处理的申请
     */
    public Optional<FriendRequest> findPendingByRequesterAndReceiver(Long requesterId, Long receiverId) {
        String sql = "SELECT id, requester_id as requesterId, receiver_id as receiverId, status, create_time as createTime, update_time as updateTime " +
                     "FROM friend_request WHERE requester_id = ? AND receiver_id = ? AND status = 'pending'";
        try {
            FriendRequest request = jdbcTemplate.queryForObject(sql, 
                new BeanPropertyRowMapper<>(FriendRequest.class), requesterId, receiverId);
            return Optional.ofNullable(request);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 检查是否存在待处理的申请
     */
    public boolean existsPendingRequest(Long requesterId, Long receiverId) {
        String sql = "SELECT COUNT(*) FROM friend_request WHERE requester_id = ? AND receiver_id = ? AND status = 'pending'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, requesterId, receiverId);
        return count != null && count > 0;
    }

    /**
     * 更新申请状态
     */
    public void updateStatus(Long id, String status) {
        String sql = "UPDATE friend_request SET status = ?, update_time = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, LocalDateTime.now(), id);
    }

    /**
     * 删除申请记录
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM friend_request WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}

