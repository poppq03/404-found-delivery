package com.found404.delivery.domain.menu.dto;

import com.found404.delivery.domain.menu.entity.Menu;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class MenuUpdateResponseDto {
    private UUID menuId;
    private String name;
    private int price;
    private String description;
    private String imageUrl;
    private boolean aiGenerated;
    private LocalDateTime updatedAt;

    public MenuUpdateResponseDto(UUID menuId, String name, int price, String description
            , String imageUrl, boolean aiGenerated, LocalDateTime updatedAt) {
        this.menuId = menuId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.aiGenerated = aiGenerated;
        this.updatedAt = updatedAt;
    }

    public static MenuUpdateResponseDto from(Menu menu) {
        return new MenuUpdateResponseDto(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getImageUrl(),
                menu.isAiGenerated(),
                menu.getUpdatedAt()
        );
    }
}
