package com.found404.delivery.domain.menu.dto;

import com.found404.delivery.domain.menu.entity.Menu;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MenuSummaryResponseDto {

    private UUID menuId;
    private String name;
    private int price;
    private String imageUrl;
    private boolean soldOut;

    public MenuSummaryResponseDto(UUID menuId, String name, int price, String imageUrl, boolean soldOut) {
        this.menuId = menuId;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.soldOut = soldOut;
    }

    public static MenuSummaryResponseDto from(Menu menu) {
        return new MenuSummaryResponseDto(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getImageUrl(),
                menu.isSoldOut()
        );
    }
}
