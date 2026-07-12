package com.found404.delivery.domain.menu.dto;

import com.found404.delivery.domain.menu.entity.Menu;
import lombok.Getter;

import java.util.UUID;

@Getter
public class MenuStatusResponseDto {

    private UUID menuId;
    private boolean hidden;
    private boolean soldOut;

    public MenuStatusResponseDto(UUID menuId, boolean hidden, boolean soldOut) {
        this.menuId = menuId;
        this.hidden = hidden;
        this.soldOut = soldOut;
    }

    public static MenuStatusResponseDto from(Menu menu) {
        return new MenuStatusResponseDto(
                menu.getId(),
                menu.isHidden(),
                menu.isSoldOut()
        );
    }
}
