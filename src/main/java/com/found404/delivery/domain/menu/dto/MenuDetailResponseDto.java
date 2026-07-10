package com.found404.delivery.domain.menu.dto;

import com.found404.delivery.domain.menu.entity.Menu;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MenuDetailResponseDto {
    private UUID menuId;
    private UUID storeId;
    private String name;
    private int price;
    private String description;
    private String imageUrl;
    private boolean hidden;
    private boolean soldOut;
    private boolean aiGenerated;

    public MenuDetailResponseDto(UUID menuId, UUID storeId, String name, int price, String description
            , String imageUrl, boolean hidden, boolean soldOut, boolean aiGenerated) {
        this.menuId = menuId;
        this.storeId = storeId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
        this.hidden = hidden;
        this.soldOut = soldOut;
        this.aiGenerated = aiGenerated;
    }

    public static MenuDetailResponseDto from(Menu menu) {
        return new MenuDetailResponseDto(
                menu.getId(),
                menu.getStoreId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getImageUrl(),
                menu.isHidden(),
                menu.isSoldOut(),
                menu.isAiGenerated()
        );
    }
}
