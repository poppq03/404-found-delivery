package com.found404.delivery.domain.menu.entity;

import com.found404.delivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(
        name = "p_menu",
        indexes = @Index(name = "idx_p_menu_store_id", columnList = "store_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Menu extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "menu_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "display_order")
    private int displayOrder = 0;

    @Column(name = "is_hidden", nullable = false)
    private boolean isHidden = false;

    @Column(name = "is_sold_out", nullable = false)
    private boolean isSoldOut = false;

    @Column(name = "is_ai_generated", nullable = false)
    private boolean isAiGenerated = false;

    @Builder
    private Menu(UUID storeId, String name, int price, String description, String imageUrl, Integer displayOrder, boolean isAiGenerated) {
        this.storeId = storeId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isAiGenerated = isAiGenerated;
    }

    public void changeStatus(Boolean isHidden, Boolean isSoldOut) {
        if (isHidden != null) this.isHidden = isHidden;
        if (isSoldOut != null) this.isSoldOut = isSoldOut;
    }
}
