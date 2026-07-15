package com.found404.delivery.domain.store.entity;


import com.found404.delivery.domain.category.entity.Category;
import com.found404.delivery.domain.region.entity.Region;
import com.found404.delivery.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;


@Table ( name = "p_store" )
@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "store_id", nullable = false, updatable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="owner_id")
    private User ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="region_id")
    private Region regionId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String description;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "detail_address", nullable = false, length = 255)
    private String detailAddress;

    @Column(name = "min_order_price")
    private Integer minOrderPrice;

    @Column(name = "delivery_fee")
    private Integer deliveryFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreStatus status;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public void update(
            String name,
            String phoneNumber,
            String description,
            String address,
            String detailAddress,
            Integer minOrderPrice,
            Integer deliveryFee,
            Category categoryId,
            Region regionId,
            Long updatedBy){
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.address = address;
        this.detailAddress = detailAddress;
        this.minOrderPrice = minOrderPrice;
        this.deliveryFee = deliveryFee;
        this.category = categoryId;
        this.regionId = regionId;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
        // 업데이트 By
    }

    public void changeImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void delete(Long deletedBy){
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    public void changeStatus(StoreStatus status) {
        this.status = status;
    }

    public void changeMinOrderPrice(Integer minOrderPrice) {
        this.minOrderPrice = minOrderPrice;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        if (this.updatedAt == null) {
            this.updatedAt = now;
        }

        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}
