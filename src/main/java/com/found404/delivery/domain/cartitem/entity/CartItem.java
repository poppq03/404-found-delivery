package com.found404.delivery.domain.cartitem.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(
        name = "p_cart_item",
        indexes = @Index(name = "idx_p_cart_item_cart_id", columnList = "cart_id"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_p_cart_item_cart_menu", columnNames = {"cart_id", "menu_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CartItem {

    @Id
    @UuidGenerator
    @Column(name = "cart_item_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    @Column(name = "menu_id", nullable = false)
    private UUID menuId;

    @Column(nullable = false)
    private int quantity;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    @Builder
    private CartItem(UUID cartId, UUID menuId, int quantity) {
        this.cartId = cartId;
        this.menuId = menuId;
        this.quantity = quantity;
    }
}