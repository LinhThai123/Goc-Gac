package com.ecommerce.gocgac.service.chat;

import com.ecommerce.gocgac.common.response.PageResponse;
import com.ecommerce.gocgac.common.util.XssSanitizer;
import com.ecommerce.gocgac.dto.chat.ChatMessageResponse;
import com.ecommerce.gocgac.dto.chat.ConversationResponse;
import com.ecommerce.gocgac.dto.chat.SendMessageRequest;
import com.ecommerce.gocgac.entity.AutoReply;
import com.ecommerce.gocgac.entity.Conversation;
import com.ecommerce.gocgac.entity.Message;
import com.ecommerce.gocgac.entity.enums.ConversationStatus;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.exception.ResourceNotFoundException;
import com.ecommerce.gocgac.repository.AutoReplyRepository;
import com.ecommerce.gocgac.repository.ConversationRepository;
import com.ecommerce.gocgac.repository.MessageRepository;
import com.ecommerce.gocgac.service.notification.NotificationService;
import com.ecommerce.gocgac.service.store.StoreResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dịch vụ tin nhắn Khách hàng ↔ Gian hàng (Sprint 8 - M16, REST).
 *
 * <p>Mỗi khách có một hội thoại với mỗi gian hàng (ràng buộc unique buyer_id + store_id).
 * Hỗ trợ đếm chưa đọc theo từng phía, đánh dấu đã đọc, và trả lời tự động theo từ khóa.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AutoReplyRepository autoReplyRepository;
    private final StoreResolver storeResolver;
    private final NotificationService notificationService;

    private static final String TYPE_MESSAGE = "MESSAGE";
    private static final String REF_CONVERSATION = "CONVERSATION";

    private enum Side { BUYER, SELLER }

    // ========== Buyer ==========

    /** Khách gửi tin tới một gian hàng (tự tạo hội thoại nếu chưa có). */
    @Transactional
    public ChatMessageResponse sendAsBuyer(Long buyerId, Long storeId, SendMessageRequest req) {
        Conversation conv = getOrCreateConversation(buyerId, storeId);
        if (conv.getStatus() == ConversationStatus.BLOCKED) {
            throw new BusinessException("Hội thoại đã bị chặn");
        }
        Message msg = saveMessage(conv.getId(), buyerId, req.getMessageText(), req.getAttachmentUrl());
        touch(conv, msg.getMessageText(), Side.SELLER); // người nhận chưa đọc là seller

        notificationService.notify(conv.getSellerId(), TYPE_MESSAGE, "Tin nhắn mới",
            "Bạn có tin nhắn mới từ khách hàng.", REF_CONVERSATION, conv.getId());

        applyAutoReply(conv, req.getMessageText());
        return ChatMessageResponse.from(msg);
    }

    public PageResponse<ConversationResponse> getMyConversations(Long buyerId, Pageable pageable) {
        return PageResponse.from(conversationRepository.findByBuyerIdOrderByLastMessageAtDesc(buyerId, pageable)
            .map(c -> ConversationResponse.from(c, nz(c.getUnreadCountBuyer()))));
    }

    // ========== Seller ==========

    /** Gian hàng trả lời một hội thoại. */
    @Transactional
    public ChatMessageResponse sendAsSeller(Long sellerUserId, Long conversationId, SendMessageRequest req) {
        Conversation conv = getConversation(conversationId);
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        if (!conv.getStoreId().equals(storeId)) {
            throw new BusinessException("Hội thoại không thuộc gian hàng của bạn");
        }
        Message msg = saveMessage(conv.getId(), sellerUserId, req.getMessageText(), req.getAttachmentUrl());
        touch(conv, msg.getMessageText(), Side.BUYER); // người nhận chưa đọc là buyer

        notificationService.notify(conv.getBuyerId(), TYPE_MESSAGE, "Tin nhắn mới",
            "Gian hàng đã trả lời tin nhắn của bạn.", REF_CONVERSATION, conv.getId());
        return ChatMessageResponse.from(msg);
    }

    public PageResponse<ConversationResponse> getStoreConversations(Long sellerUserId, Pageable pageable) {
        Long storeId = storeResolver.resolveStoreId(sellerUserId);
        return PageResponse.from(conversationRepository.findByStoreIdOrderByLastMessageAtDesc(storeId, pageable)
            .map(c -> ConversationResponse.from(c, nz(c.getUnreadCountSeller()))));
    }

    // ========== Shared ==========

    /** Lấy tin nhắn của hội thoại + đánh dấu đã đọc cho phía đang xem. */
    @Transactional
    public PageResponse<ChatMessageResponse> getMessages(Long userId, Long conversationId, Pageable pageable) {
        Conversation conv = getConversation(conversationId);
        Side side = determineSide(conv, userId);
        markRead(conv, userId, side);
        return PageResponse.from(
            messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(ChatMessageResponse::from));
    }

    @Transactional
    public void markConversationRead(Long userId, Long conversationId) {
        Conversation conv = getConversation(conversationId);
        Side side = determineSide(conv, userId);
        markRead(conv, userId, side);
    }

    @Transactional
    public void archive(Long userId, Long conversationId) {
        Conversation conv = getConversation(conversationId);
        determineSide(conv, userId); // chỉ thành viên hội thoại mới được lưu trữ
        conv.setStatus(ConversationStatus.ARCHIVED);
        conversationRepository.save(conv);
    }

    // ========== Helpers ==========

    private Conversation getOrCreateConversation(Long buyerId, Long storeId) {
        return conversationRepository.findByBuyerIdAndStoreId(buyerId, storeId)
            .orElseGet(() -> {
                Long sellerId = storeResolver.resolveStoreOwnerUserId(storeId);
                if (sellerId.equals(buyerId)) {
                    throw new BusinessException("Không thể nhắn tin với gian hàng của chính mình");
                }
                Conversation c = new Conversation();
                c.setBuyerId(buyerId);
                c.setStoreId(storeId);
                c.setSellerId(sellerId);
                c.setStatus(ConversationStatus.ACTIVE);
                return conversationRepository.save(c);
            });
    }

    private Message saveMessage(Long conversationId, Long senderId, String text, String attachmentUrl) {
        Message m = new Message();
        m.setConversationId(conversationId);
        m.setSenderId(senderId);
        m.setMessageText(XssSanitizer.sanitize(text));
        m.setAttachmentUrl(attachmentUrl);
        m.setIsRead(false);
        return messageRepository.save(m);
    }

    /** Cập nhật hội thoại sau khi có tin mới; tăng số chưa đọc cho phía nhận. */
    private void touch(Conversation conv, String lastMessage, Side unreadFor) {
        conv.setLastMessage(lastMessage);
        conv.setLastMessageAt(LocalDateTime.now());
        if (unreadFor == Side.SELLER) {
            conv.setUnreadCountSeller(nz(conv.getUnreadCountSeller()) + 1);
        } else {
            conv.setUnreadCountBuyer(nz(conv.getUnreadCountBuyer()) + 1);
        }
        if (conv.getStatus() == ConversationStatus.ARCHIVED) {
            conv.setStatus(ConversationStatus.ACTIVE);
        }
        conversationRepository.save(conv);
    }

    private void applyAutoReply(Conversation conv, String buyerText) {
        List<AutoReply> replies = autoReplyRepository.findByStoreIdAndIsActiveTrue(conv.getStoreId());
        String lower = buyerText != null ? buyerText.toLowerCase() : "";
        for (AutoReply ar : replies) {
            String kw = ar.getTriggerKeyword();
            boolean match = !StringUtils.hasText(kw) || lower.contains(kw.toLowerCase());
            if (match) {
                Message reply = saveMessage(conv.getId(), conv.getSellerId(), ar.getReplyMessage(), null);
                touch(conv, reply.getMessageText(), Side.BUYER);
                notificationService.notify(conv.getBuyerId(), TYPE_MESSAGE, "Tin nhắn mới",
                    "Gian hàng đã trả lời tin nhắn của bạn.", REF_CONVERSATION, conv.getId());
                break; // chỉ gửi một auto-reply
            }
        }
    }

    private Side determineSide(Conversation conv, Long userId) {
        if (conv.getBuyerId().equals(userId)) {
            return Side.BUYER;
        }
        try {
            Long storeId = storeResolver.resolveStoreId(userId);
            if (conv.getStoreId().equals(storeId)) {
                return Side.SELLER;
            }
        } catch (RuntimeException ignored) {
            // người dùng không sở hữu gian hàng → không phải seller
        }
        throw new BusinessException("Bạn không có quyền truy cập hội thoại này");
    }

    private void markRead(Conversation conv, Long readerId, Side side) {
        messageRepository.markReadByReader(conv.getId(), readerId, LocalDateTime.now());
        if (side == Side.BUYER) {
            conv.setUnreadCountBuyer(0);
        } else {
            conv.setUnreadCountSeller(0);
        }
        conversationRepository.save(conv);
    }

    private Conversation getConversation(Long id) {
        return conversationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hội thoại không tồn tại"));
    }

    private int nz(Integer value) {
        return value != null ? value : 0;
    }
}
