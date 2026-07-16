package com.found404.delivery.domain.store.entity;


import com.found404.delivery.domain.category.entity.Category;
import com.found404.delivery.domain.region.entity.Region;
import com.found404.delivery.domain.user.entity.User;
import com.found404.delivery.global.entity.BaseEntity;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "p_store")
@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Store extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "store_id", nullable = false, updatable = false)
    private UUID storeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "detail_address", nullable = false, length = 255)
    private String detailAddress;

    @Column(name = "min_order_price", nullable = false)
    private Integer minOrderPrice;

    @Column(name = "delivery_fee", nullable = false)
    private Integer deliveryFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoreStatus status;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

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
            Category category,
            Region region
    ) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.description = description;
        this.address = address;
        this.detailAddress = detailAddress;
        this.minOrderPrice = minOrderPrice;
        this.deliveryFee = deliveryFee;
        this.category = category;
        this.region = region;
    }

    public void changeImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void delete(Long deletedBy) {
        this.isActive = false;
        this.status = StoreStatus.SUSPENDED;
        markDeleted(deletedBy);
    }

    public void changeStatus(StoreStatus status) {
        this.status = status;
    }

    public void changeMinOrderPrice(Integer minOrderPrice) {
        this.minOrderPrice = minOrderPrice;
    }

    @PrePersist
    private void prePersist() {
        if (this.isActive == null) {
            this.isActive = true;
        }

        if (this.status == null) {
            this.status = StoreStatus.PENDING;
        }
    }

    public void suspend() {
        validatePendingStatus();
        this.status = StoreStatus.SUSPENDED;
    }

    public void open() {
        validatePendingStatus();
        this.status = StoreStatus.OPEN;
    }

    public void close() {
        validatePendingStatus();
        this.status = StoreStatus.CLOSED;
    }


    public void approve() {
        validatePendingStatus();
        this.status = StoreStatus.OPEN;
    }


    private void validatePendingStatus() {
        if (this.status != StoreStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_ADDRESS);
        }
    }
}

