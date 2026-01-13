package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    
    /**
     * Tìm channel theo slug
     */
    Optional<Channel> findByChannelSlug(String channelSlug);
    
    /**
     * Tìm channel theo store_id
     */
    Optional<Channel> findByStoreId(Long storeId);
    
    /**
     * Tìm channel theo cooperative_id
     */
    Optional<Channel> findByCooperativeId(Long cooperativeId);
    
    /**
     * Tìm tất cả channels của một store
     */
    List<Channel> findAllByStoreId(Long storeId);
    
    /**
     * Tìm tất cả channels active
     */
    List<Channel> findAllByIsActiveTrue();
    
    /**
     * Tìm tất cả channels featured
     */
    List<Channel> findAllByIsFeaturedTrueAndIsActiveTrue();
    
    /**
     * Kiểm tra slug đã tồn tại chưa
     */
    boolean existsByChannelSlug(String channelSlug);
    
    /**
     * Kiểm tra store đã có channel chưa
     */
    boolean existsByStoreId(Long storeId);
}

