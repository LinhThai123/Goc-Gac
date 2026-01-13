package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.ChannelFollow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelFollowRepository extends JpaRepository<ChannelFollow, Long> {
    
    /**
     * Tìm follow theo channel_id và user_id
     */
    Optional<ChannelFollow> findByChannelIdAndUserId(Long channelId, Long userId);
    
    /**
     * Kiểm tra user đã follow channel chưa
     */
    boolean existsByChannelIdAndUserId(Long channelId, Long userId);
    
    /**
     * Tìm tất cả channels mà user đã follow
     */
    List<ChannelFollow> findAllByUserId(Long userId);
    
    /**
     * Tìm tất cả users đã follow channel
     */
    List<ChannelFollow> findAllByChannelId(Long channelId);
    
    /**
     * Đếm số lượng followers của channel
     */
    long countByChannelId(Long channelId);
    
    /**
     * Xóa follow
     */
    void deleteByChannelIdAndUserId(Long channelId, Long userId);
}

