package com.ecommerce.gocgac.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_events", indexes = @Index(name = "idx_shipping_events_tracking", columnList = "tracking_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tracking_id", nullable = false)
    private Long trackingId;
    
    @Column(name = "event_status", nullable = false, length = 100)
    private String eventStatus;
    
    @Column(name = "event_description", columnDefinition = "TEXT")
    private String eventDescription;
    
    @Column(length = 255)
    private String location;
    
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

