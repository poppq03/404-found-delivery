package com.found404.delivery.domain.menu.dto;

import com.found404.delivery.domain.menu.entity.Menu;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class MenuCreateResponseDto {

    private UUID menuId;
    private String name;
    private int price;
    private String description;
    private String imageUrl;
    private boolean isAiGenerated;
    private LocalDateTime createdAt;

    public MenuCreateResponseDto(UUID menuId, String name, int price, String description
            , String imageUrl, boolean isAiGenerated, LocalDateTime createdAt) {
        this.menuId = menuId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isAiGenerated = isAiGenerated;
        this.createdAt = createdAt;
    }

    public static MenuCreateResponseDto from(Menu menu) {
        return new MenuCreateResponseDto(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getImageUrl(),
                menu.isAiGenerated(),
                menu.getCreatedAt()
        );
    }
}
