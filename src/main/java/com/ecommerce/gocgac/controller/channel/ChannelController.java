package com.ecommerce.gocgac.controller.channel;

import com.ecommerce.gocgac.common.response.MessageResponse;
import com.ecommerce.gocgac.common.util.JwtUtils;
import com.ecommerce.gocgac.dto.channel.ChannelResponse;
import com.ecommerce.gocgac.dto.channel.CreateChannelRequest;
import com.ecommerce.gocgac.dto.channel.UpdateChannelRequest;
import com.ecommerce.gocgac.entity.User;
import com.ecommerce.gocgac.exception.ChannelException;
import com.ecommerce.gocgac.repository.UserRepository;
import com.ecommerce.gocgac.service.channel.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller cho Channel (Micro-site riêng cho shop)
 * - Public endpoints: Xem channel theo slug, list channels
 * - Protected endpoints: CRUD channel của mình
 */
//TODO đang làm dở cập nhật thông tin channel 
@Slf4j
@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@Tag(name = "Channel", description = "API quản lý Channel (Micro-site cho shop)")
@SecurityRequirement(name = "bearerAuth")
public class ChannelController {
    
    private final ChannelService channelService;
    private final UserRepository userRepository;
    
    /**
     * Lấy userId từ JWT token
     */
    private Long getCurrentUserId() {
        String email = JwtUtils.getEmail();
        if (email == null) {
            throw new ChannelException("Không thể xác định user từ token");
        }
        return userRepository.findByEmail(email)
            .map(User::getId)
            .orElseThrow(() -> new ChannelException("User không tồn tại"));
    }
    
    // ========== Public Endpoints ==========
    
    /**
     * Lấy channel theo slug (public)
     */
    @GetMapping("/{slug}")
    @Operation(summary = "Lấy channel theo slug", 
               description = "Lấy thông tin channel công khai theo slug (public endpoint)")
    public ResponseEntity<MessageResponse> getChannelBySlug(@PathVariable String slug) {
        Optional<ChannelResponse> channel = channelService.getChannelBySlug(slug);
        
        MessageResponse response = new MessageResponse();
        if (channel.isPresent()) {
            // Tăng view count
            // TODO: Implement view count tracking
            
            response.setMessage("Lấy thông tin channel thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(channel.get());
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Không tìm thấy channel với slug: " + slug);
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * Lấy danh sách channels active (public)
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách channels", 
               description = "Lấy danh sách tất cả channels đang active (public endpoint)")
    public ResponseEntity<MessageResponse> getAllChannels() {
        List<ChannelResponse> channels = channelService.getAllActiveChannels();
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lấy danh sách channels thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(channels);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy danh sách channels featured (public)
     */
    @GetMapping("/featured")
    @Operation(summary = "Lấy danh sách channels featured", 
               description = "Lấy danh sách channels được đánh dấu featured (public endpoint)")
    public ResponseEntity<MessageResponse> getFeaturedChannels() {
        List<ChannelResponse> channels = channelService.getFeaturedChannels();
        
        MessageResponse response = new MessageResponse();
        response.setMessage("Lấy danh sách channels featured thành công");
        response.setStatus(HttpStatus.OK.value());
        response.setData(channels);
        return ResponseEntity.ok(response);
    }
    
    // ========== Protected Endpoints ==========
    
    /**
     * Lấy channel của mình (multi-tenant)
     */
    @GetMapping("/my-channel")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Lấy channel của tôi", 
               description = "Lấy thông tin channel của store hiện tại (yêu cầu COOPERATIVE_MANAGER hoặc SELLER)")
    public ResponseEntity<MessageResponse> getMyChannel() {
        Long userId = getCurrentUserId();
        Optional<ChannelResponse> channel = channelService.getMyChannel(userId);
        
        MessageResponse response = new MessageResponse();
        if (channel.isPresent()) {
            response.setMessage("Lấy thông tin channel thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(channel.get());
            return ResponseEntity.ok(response);
        } else {
            response.setMessage("Bạn chưa có channel");
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * Tạo channel mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Tạo channel mới", 
               description = "Tạo channel mới cho store của bạn (yêu cầu COOPERATIVE_MANAGER hoặc SELLER)")
    public ResponseEntity<MessageResponse> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        try {
            Long userId = getCurrentUserId();
            ChannelResponse channel = channelService.createChannel(userId, request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Tạo channel thành công");
            response.setStatus(HttpStatus.CREATED.value());
            response.setData(channel);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ChannelException e) {
            log.error("Error creating channel: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Cập nhật channel
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Cập nhật channel", 
               description = "Cập nhật thông tin channel của bạn (yêu cầu COOPERATIVE_MANAGER hoặc SELLER)")
    public ResponseEntity<MessageResponse> updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateChannelRequest request) {
        try {
            Long userId = getCurrentUserId();
            ChannelResponse channel = channelService.updateChannel(id, userId, request);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Cập nhật channel thành công");
            response.setStatus(HttpStatus.OK.value());
            response.setData(channel);
            return ResponseEntity.ok(response);
        } catch (ChannelException e) {
            log.error("Error updating channel: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Xóa channel (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('COOPERATIVE_MANAGER', 'SELLER', 'SUPER_ADMIN')")
    @Operation(summary = "Xóa channel", 
               description = "Xóa channel của bạn (soft delete - set isActive = false)")
    public ResponseEntity<MessageResponse> deleteChannel(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            channelService.deleteChannel(id, userId);
            
            MessageResponse response = new MessageResponse();
            response.setMessage("Xóa channel thành công");
            response.setStatus(HttpStatus.OK.value());
            return ResponseEntity.ok(response);
        } catch (ChannelException e) {
            log.error("Error deleting channel: {}", e.getMessage());
            MessageResponse response = new MessageResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

