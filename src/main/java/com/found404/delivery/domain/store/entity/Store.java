package com.found404.delivery.domain.store.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table ( name = "p_store" )
@Getter
@NoArgsConstructor
public class Store {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private UUID storeId;

    @Column(name = "owner_id", nullable = false)
    private Integer ownerId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "region_id", nullable = false)
    private UUID regionId;

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

    @Column
    private String status;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Integer deletedBy;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

}
