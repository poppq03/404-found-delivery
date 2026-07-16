package com.found404.delivery.domain.cart.entity;

import com.found404.delivery.domain.cartitem.entity.CartItem;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "p_cart",
        uniqueConstraints = @UniqueConstraint(name = "uk_p_cart_user_id", columnNames = "user_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Cart {

    @Id
    @UuidGenerator
    @Column(name = "cart_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "store_id")
    private UUID storeId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

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
    private Cart(Long userId, UUID storeId) {
        this.userId = userId;
        this.storeId = storeId;
    }

    public void addItem(UUID menuId, int quantity, UUID menuStoreId) {
        if (this.storeId == null) {          // 빈 장바구니면 가게 지정
            this.storeId = menuStoreId;
        }
        CartItem existing = findItem(menuId);
        if (existing != null) {
            existing.addQuantity(quantity);  // 같은 메뉴 → 수량 합산
        } else {
            CartItem item = CartItem.builder().menuId(menuId).quantity(quantity).build();
            this.items.add(item);            // 새 메뉴 → 항목 추가
            item.assignCart(this);           // 연관관계 양쪽 세팅
        }
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        if (items.isEmpty()) {
            this.storeId = null;
        }
    }

    public void clearCart() {
        items.clear();
        this.storeId = null;
    }

    // items에서 같은 메뉴 항목 찾기 (없으면 null)
    private CartItem findItem(UUID menuId) {
        return this.items.stream()
                .filter(item -> item.getMenuId().equals(menuId))
                .findFirst()
                .orElse(null);
    }
}