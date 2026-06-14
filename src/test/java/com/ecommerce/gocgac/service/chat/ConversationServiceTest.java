package com.ecommerce.gocgac.service.chat;

import com.ecommerce.gocgac.dto.chat.ChatMessageResponse;
import com.ecommerce.gocgac.dto.chat.SendMessageRequest;
import com.ecommerce.gocgac.entity.AutoReply;
import com.ecommerce.gocgac.entity.Conversation;
import com.ecommerce.gocgac.entity.Message;
import com.ecommerce.gocgac.entity.enums.ConversationStatus;
import com.ecommerce.gocgac.exception.BusinessException;
import com.ecommerce.gocgac.repository.AutoReplyRepository;
import com.ecommerce.gocgac.repository.ConversationRepository;
import com.ecommerce.gocgac.repository.MessageRepository;
import com.ecommerce.gocgac.service.notification.NotificationService;
import com.ecommerce.gocgac.service.store.StoreResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private AutoReplyRepository autoReplyRepository;
    @Mock private StoreResolver storeResolver;
    @Mock private NotificationService notificationService;

    @InjectMocks private ConversationService conversationService;

    private static final Long BUYER_ID = 7L;
    private static final Long SELLER_ID = 5L;
    private static final Long STORE_ID = 1L;
    private static final Long CONV_ID = 50L;

    @BeforeEach
    void stubSaves() {
        lenient().when(messageRepository.save(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            if (m.getId() == null) m.setId(100L);
            return m;
        });
        lenient().when(conversationRepository.save(any(Conversation.class))).thenAnswer(inv -> {
            Conversation c = inv.getArgument(0);
            if (c.getId() == null) c.setId(CONV_ID);
            return c;
        });
    }

    private Conversation existingConversation() {
        Conversation c = new Conversation();
        c.setId(CONV_ID);
        c.setBuyerId(BUYER_ID);
        c.setSellerId(SELLER_ID);
        c.setStoreId(STORE_ID);
        c.setStatus(ConversationStatus.ACTIVE);
        c.setUnreadCountBuyer(0);
        c.setUnreadCountSeller(0);
        return c;
    }

    private SendMessageRequest msg(String text) {
        SendMessageRequest r = new SendMessageRequest();
        r.setMessageText(text);
        return r;
    }

    @Test
    @DisplayName("Khách gửi tin lần đầu: tạo hội thoại, +1 chưa đọc cho seller, thông báo seller")
    void sendAsBuyer_createsConversation() {
        when(conversationRepository.findByBuyerIdAndStoreId(BUYER_ID, STORE_ID)).thenReturn(Optional.empty());
        when(storeResolver.resolveStoreOwnerUserId(STORE_ID)).thenReturn(SELLER_ID);
        when(autoReplyRepository.findByStoreIdAndIsActiveTrue(STORE_ID)).thenReturn(List.of());

        ChatMessageResponse res = conversationService.sendAsBuyer(BUYER_ID, STORE_ID, msg("Xin chào shop"));

        assertThat(res.getSenderId()).isEqualTo(BUYER_ID);

        ArgumentCaptor<Conversation> captor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository, atLeast(1)).save(captor.capture());
        Conversation last = captor.getValue();
        assertThat(last.getSellerId()).isEqualTo(SELLER_ID);
        assertThat(last.getUnreadCountSeller()).isEqualTo(1);

        verify(notificationService).notify(eq(SELLER_ID), eq("MESSAGE"), any(), any(), any(), eq(CONV_ID));
    }

    @Test
    @DisplayName("Auto-reply khớp từ khóa: gửi thêm tin trả lời cho khách")
    void sendAsBuyer_autoReply() {
        when(conversationRepository.findByBuyerIdAndStoreId(BUYER_ID, STORE_ID))
            .thenReturn(Optional.of(existingConversation()));
        AutoReply ar = new AutoReply();
        ar.setStoreId(STORE_ID);
        ar.setTriggerKeyword("giá");
        ar.setReplyMessage("Giá sản phẩm có trên trang chi tiết ạ");
        ar.setIsActive(true);
        when(autoReplyRepository.findByStoreIdAndIsActiveTrue(STORE_ID)).thenReturn(List.of(ar));

        conversationService.sendAsBuyer(BUYER_ID, STORE_ID, msg("Cho hỏi giá bao nhiêu"));

        // 1 tin của khách + 1 tin auto-reply
        verify(messageRepository, times(2)).save(any(Message.class));
        // auto-reply thông báo cho buyer
        verify(notificationService).notify(eq(BUYER_ID), eq("MESSAGE"), any(), any(), any(), eq(CONV_ID));
    }

    @Test
    @DisplayName("Seller trả lời hội thoại của gian hàng khác → BusinessException")
    void sendAsSeller_wrongStore_throws() {
        when(conversationRepository.findById(CONV_ID)).thenReturn(Optional.of(existingConversation()));
        when(storeResolver.resolveStoreId(SELLER_ID)).thenReturn(999L); // khác store của hội thoại

        assertThatThrownBy(() -> conversationService.sendAsSeller(SELLER_ID, CONV_ID, msg("Chào bạn")))
            .isInstanceOf(BusinessException.class);

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Người ngoài hội thoại xem tin → BusinessException")
    void getMessages_notMember_throws() {
        Long stranger = 99L;
        when(conversationRepository.findById(CONV_ID)).thenReturn(Optional.of(existingConversation()));
        when(storeResolver.resolveStoreId(stranger)).thenThrow(new BusinessException("Bạn chưa có gian hàng"));

        assertThatThrownBy(() ->
            conversationService.getMessages(stranger, CONV_ID, org.springframework.data.domain.PageRequest.of(0, 30)))
            .isInstanceOf(BusinessException.class);
    }
}
