package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations", 
    uniqueConstraints = @UniqueConstraint(name = "unique_conversation", columnNames = {"buyer_id", "store_id"}),
    indexes = {
        @Index(name = "idx_buyer", columnList = "buyer_id"),
        @Index(name = "idx_conversations_seller", columnList = "seller_id"),
        @Index(name = "idx_conversations_store", columnList = "store_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;
    
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    
    @Column(name = "store_id", nullable = false)
    private Long storeId;
    
    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;
    
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;
    
    @Column(name = "unread_count_buyer", nullable = false)
    private Integer unreadCountBuyer = 0;
    
    @Column(name = "unread_count_seller", nullable = false)
    private Integer unreadCountSeller = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status = ConversationStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

