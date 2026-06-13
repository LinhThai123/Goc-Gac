package com.ecommerce.gocgac.service.notification;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.dto.notification.NotificationResponse;
import com.ecommerce.gocgac.entity.Notification;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Dịch vụ thông báo trong ứng dụng (in-app notification).
 *
 * <p>Hạ tầng dùng chung: các module khác (đơn hàng, kiểm duyệt, khuyến mãi)
 * gọi {@link #notify} để gửi thông báo cho người dùng.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Tạo và lưu một thông báo cho người dùng.
     *
     * @param userId        người nhận
     * @param type          loại thông báo (vd: ORDER, PRODUCT_APPROVAL, PROMOTION)
     * @param title         tiêu đề
     * @param message       nội dung
     * @param referenceType loại đối tượng liên quan (vd: ORDER) — có thể null
     * @param referenceId   id đối tượng liên quan — có thể null
     */
    @Transactional
    public Notification notify(Long userId, String type, String title, String message,
                               String referenceType, Long referenceId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setNotificationType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setReferenceType(referenceType);
        n.setReferenceId(referenceId);
        n.setIsRead(false);
        n = notificationRepository.save(n);
        log.debug("Notification {} ({}) created for user {}", n.getId(), type, userId);
        return n;
    }

    /** Lấy danh sách thông báo của người dùng (mới nhất trước), có phân trang. */
    public PageResponse<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        return PageResponse.from(
            notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from)
        );
    }

    /** Đếm số thông báo chưa đọc. */
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /** Đánh dấu một thông báo là đã đọc (chỉ chủ sở hữu). */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findByIdAndUserId(notificationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại"));
        if (Boolean.FALSE.equals(n.getIsRead())) {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        }
    }

    /** Đánh dấu tất cả thông báo của người dùng là đã đọc. */
    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }
}
