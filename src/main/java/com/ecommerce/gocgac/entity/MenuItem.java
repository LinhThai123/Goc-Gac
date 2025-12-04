package com.ecommerce.gocgac.entity;

import com.ecommerce.gocgac.entity.enums.MenuType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "menu_items", indexes = {
    @Index(name = "idx_menu_items_parent", columnList = "parent_id"),
    @Index(name = "idx_menu_items_type", columnList = "menu_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "menu_type", nullable = false)
    private MenuType menuType;
    
    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;
    
    @Column(name = "menu_url", length = 255)
    private String menuUrl;
    
    @Column(length = 100)
    private String icon;
    
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
    
    @Column(name = "permission_code", length = 50)
    private String permissionCode;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

