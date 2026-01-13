package com.ecommerce.gocgac.service.channel;

import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.channel.ChannelResponse;
import com.ecommerce.gocgac.dto.channel.CreateChannelRequest;
import com.ecommerce.gocgac.dto.channel.UpdateChannelRequest;
import com.ecommerce.gocgac.entity.Channel;
import com.ecommerce.gocgac.entity.Cooperative;
import com.ecommerce.gocgac.entity.Store;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.entity.enums.UserType;
import com.ecommerce.gocgac.exception.ChannelException;
import com.ecommerce.gocgac.repository.ChannelRepository;
import com.ecommerce.gocgac.repository.CooperativeRepository;
import com.ecommerce.gocgac.repository.StoreRepository;
import com.ecommerce.gocgac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {
    
    private final ChannelRepository channelRepository;
    private final StoreRepository storeRepository;
    private final CooperativeRepository cooperativeRepository;
    private final UserRepository userRepository;
    
    /**
     * Lấy storeId từ userId
     * - Nếu user là COOPERATIVE_MANAGER: lấy store qua cooperative
     * - Nếu user là SELLER: lấy store qua sellerId
     */
    private Long getStoreIdByUserId(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ChannelException("User không tồn tại"));
        
        if (user.getUserType() == UserType.COOPERATIVE_MANAGER) {
            // Lấy cooperative của user
            Cooperative cooperative = cooperativeRepository.findByUserId(userId)
                .orElseThrow(() -> new ChannelException("Bạn chưa có HTX"));
            
            // Lấy store của cooperative
            Store store = storeRepository.findByCooperativeId(cooperative.getId())
                .orElseThrow(() -> new ChannelException("HTX của bạn chưa có Store"));
            
            return store.getId();
        } else if (user.getUserType() == UserType.SELLER) {
            // Lấy store của seller
            Store store = storeRepository.findBySellerId(userId)
                .orElseThrow(() -> new ChannelException("Bạn chưa có Store"));
            
            return store.getId();
        } else {
            throw new ChannelException("Chỉ COOPERATIVE_MANAGER hoặc SELLER mới có thể tạo Channel");
        }
    }
    
    /**
     * Validate ownership - kiểm tra channel thuộc về store của user
     */
    private void validateOwnership(Long channelId, Long userId) {
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ChannelException("Channel không tồn tại"));
        
        Long userStoreId = getStoreIdByUserId(userId);
        
        if (!channel.getStoreId().equals(userStoreId)) {
            throw new ChannelException("Bạn không có quyền truy cập channel này");
        }
    }
    
    /**
     * Tạo channel mới
     */
    @Transactional
    public ChannelResponse createChannel(Long userId, CreateChannelRequest request) {
        // Lấy storeId từ userId
        Long storeId = getStoreIdByUserId(userId);
        
        // Kiểm tra store đã có channel chưa
        if (channelRepository.existsByStoreId(storeId)) {
            throw new ChannelException("Store đã có channel. Mỗi store chỉ có thể có một channel");
        }
        
        // Kiểm tra slug đã tồn tại chưa
        if (channelRepository.existsByChannelSlug(request.getChannelSlug())) {
            throw new ChannelException("Channel slug đã tồn tại: " + request.getChannelSlug());
        }
        
        // Lấy cooperativeId nếu có
        Long cooperativeId = null;
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ChannelException("Store không tồn tại"));
        
        if (store.getCooperativeId() != null) {
            cooperativeId = store.getCooperativeId();
        }
        
        // Tạo channel mới
        Channel channel = new Channel();
        channel.setStoreId(storeId);
        channel.setCooperativeId(cooperativeId);
        channel.setChannelSlug(XssSanitizer.sanitizeStructuredData(request.getChannelSlug()));
        channel.setChannelName(XssSanitizer.sanitize(request.getChannelName()));
        channel.setShortDescription(XssSanitizer.sanitize(request.getShortDescription()));
        channel.setDescription(XssSanitizer.sanitizeHtml(request.getDescription()));
        channel.setBannerUrl(XssSanitizer.sanitizeStructuredData(request.getBannerUrl()));
        channel.setLogoUrl(XssSanitizer.sanitizeStructuredData(request.getLogoUrl()));
        channel.setVideoUrl(XssSanitizer.sanitizeStructuredData(request.getVideoUrl()));
        channel.setStoryContent(XssSanitizer.sanitizeHtml(request.getStoryContent()));
        channel.setOriginStory(XssSanitizer.sanitizeHtml(request.getOriginStory()));
        channel.setThemeSettings(request.getThemeSettings()); // JSON string, không cần sanitize
        channel.setMetaTitle(XssSanitizer.sanitize(request.getMetaTitle()));
        channel.setMetaDescription(XssSanitizer.sanitize(request.getMetaDescription()));
        channel.setMetaKeywords(XssSanitizer.sanitize(request.getMetaKeywords()));
        channel.setIsActive(true);
        channel.setIsFeatured(false);
        
        channel = channelRepository.save(channel);
        
        log.info("Channel {} created for Store {}", channel.getId(), storeId);
        
        return convertToResponse(channel);
    }
    
    /**
     * Lấy channel theo slug (public)
     */
    public Optional<ChannelResponse> getChannelBySlug(String slug) {
        return channelRepository.findByChannelSlug(slug)
            .filter(Channel::getIsActive)
            .map(this::convertToResponse);
    }
    
    /**
     * Lấy channel của store hiện tại (multi-tenant)
     */
    public Optional<ChannelResponse> getMyChannel(Long userId) {
        try {
            Long storeId = getStoreIdByUserId(userId);
            return channelRepository.findByStoreId(storeId)
                .map(this::convertToResponse);
        } catch (ChannelException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Lấy channel theo ID (với ownership validation)
     */
    public ChannelResponse getChannelById(Long channelId, Long userId) {
        validateOwnership(channelId, userId);
        
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ChannelException("Channel không tồn tại"));
        
        return convertToResponse(channel);
    }
    
    /**
     * Cập nhật channel
     */
    @Transactional
    public ChannelResponse updateChannel(Long channelId, Long userId, UpdateChannelRequest request) {
        validateOwnership(channelId, userId);
        
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ChannelException("Channel không tồn tại"));
        
        // Cập nhật các trường
        if (request.getChannelSlug() != null) {
            // Kiểm tra slug mới đã tồn tại chưa (trừ channel hiện tại)
            if (!request.getChannelSlug().equals(channel.getChannelSlug()) &&
                channelRepository.existsByChannelSlug(request.getChannelSlug())) {
                throw new ChannelException("Channel slug đã tồn tại: " + request.getChannelSlug());
            }
            channel.setChannelSlug(XssSanitizer.sanitizeStructuredData(request.getChannelSlug()));
        }
        
        if (request.getChannelName() != null) {
            channel.setChannelName(XssSanitizer.sanitize(request.getChannelName()));
        }
        
        if (request.getShortDescription() != null) {
            channel.setShortDescription(XssSanitizer.sanitize(request.getShortDescription()));
        }
        
        if (request.getDescription() != null) {
            channel.setDescription(XssSanitizer.sanitizeHtml(request.getDescription()));
        }
        
        if (request.getBannerUrl() != null) {
            channel.setBannerUrl(XssSanitizer.sanitizeStructuredData(request.getBannerUrl()));
        }
        
        if (request.getLogoUrl() != null) {
            channel.setLogoUrl(XssSanitizer.sanitizeStructuredData(request.getLogoUrl()));
        }
        
        if (request.getVideoUrl() != null) {
            channel.setVideoUrl(XssSanitizer.sanitizeStructuredData(request.getVideoUrl()));
        }
        
        if (request.getStoryContent() != null) {
            channel.setStoryContent(XssSanitizer.sanitizeHtml(request.getStoryContent()));
        }
        
        if (request.getOriginStory() != null) {
            channel.setOriginStory(XssSanitizer.sanitizeHtml(request.getOriginStory()));
        }
        
        if (request.getThemeSettings() != null) {
            channel.setThemeSettings(request.getThemeSettings());
        }
        
        if (request.getIsActive() != null) {
            channel.setIsActive(request.getIsActive());
        }
        
        if (request.getIsFeatured() != null) {
            channel.setIsFeatured(request.getIsFeatured());
        }
        
        if (request.getMetaTitle() != null) {
            channel.setMetaTitle(XssSanitizer.sanitize(request.getMetaTitle()));
        }
        
        if (request.getMetaDescription() != null) {
            channel.setMetaDescription(XssSanitizer.sanitize(request.getMetaDescription()));
        }
        
        if (request.getMetaKeywords() != null) {
            channel.setMetaKeywords(XssSanitizer.sanitize(request.getMetaKeywords()));
        }
        
        channel = channelRepository.save(channel);
        
        log.info("Channel {} updated by user {}", channelId, userId);
        
        return convertToResponse(channel);
    }
    
    /**
     * Xóa channel (soft delete - set isActive = false)
     */
    @Transactional
    public void deleteChannel(Long channelId, Long userId) {
        validateOwnership(channelId, userId);
        
        Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new ChannelException("Channel không tồn tại"));
        
        channel.setIsActive(false);
        channelRepository.save(channel);
        
        log.info("Channel {} deactivated by user {}", channelId, userId);
    }
    
    /**
     * Lấy tất cả channels active (public)
     */
    public List<ChannelResponse> getAllActiveChannels() {
        return channelRepository.findAllByIsActiveTrue().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Lấy tất cả channels featured (public)
     */
    public List<ChannelResponse> getFeaturedChannels() {
        return channelRepository.findAllByIsFeaturedTrueAndIsActiveTrue().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert Channel entity to ChannelResponse DTO
     */
    private ChannelResponse convertToResponse(Channel channel) {
        ChannelResponse response = new ChannelResponse();
        response.setId(channel.getId());
        response.setStoreId(channel.getStoreId());
        response.setCooperativeId(channel.getCooperativeId());
        response.setChannelSlug(channel.getChannelSlug());
        response.setChannelName(channel.getChannelName());
        response.setShortDescription(channel.getShortDescription());
        response.setDescription(channel.getDescription());
        response.setBannerUrl(channel.getBannerUrl());
        response.setLogoUrl(channel.getLogoUrl());
        response.setVideoUrl(channel.getVideoUrl());
        response.setStoryContent(channel.getStoryContent());
        response.setOriginStory(channel.getOriginStory());
        response.setThemeSettings(channel.getThemeSettings());
        response.setFollowerCount(channel.getFollowerCount());
        response.setViewCount(channel.getViewCount());
        response.setIsActive(channel.getIsActive());
        response.setIsFeatured(channel.getIsFeatured());
        response.setMetaTitle(channel.getMetaTitle());
        response.setMetaDescription(channel.getMetaDescription());
        response.setMetaKeywords(channel.getMetaKeywords());
        response.setCreatedAt(channel.getCreatedAt());
        response.setUpdatedAt(channel.getUpdatedAt());
        return response;
    }
}

