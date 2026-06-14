package com.ecommerce.gocgac.repository;

import com.ecommerce.gocgac.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    /** Đánh dấu đã đọc các tin trong hội thoại KHÔNG do chính người đọc gửi. */
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :now " +
           "WHERE m.conversationId = :conversationId AND m.senderId <> :readerId AND m.isRead = false")
    int markReadByReader(@Param("conversationId") Long conversationId,
                         @Param("readerId") Long readerId,
                         @Param("now") LocalDateTime now);
}
