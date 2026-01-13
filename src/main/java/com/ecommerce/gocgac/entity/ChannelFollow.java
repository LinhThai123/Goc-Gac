package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity cho Channel Follow (Social feature)
 * User có thể follow channel để nhận notification
 */
@Entity
@Table(name = "channel_follows", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"channel_id", "user_id"}),
       indexes = {
           @Index(name = "idx_channel_follows_channel", columnList = "channel_id"),
           @Index(name = "idx_channel_follows_user", columnList = "user_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelFollow {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "channel_id", nullable = false)
    private Long channelId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "followed_at", nullable = false, updatable = false)
    private LocalDateTime followedAt = LocalDateTime.now();
}

