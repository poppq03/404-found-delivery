package com.found404.delivery.domain.menu.dto;

import com.found404.delivery.domain.menu.entity.Menu;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class MenuDeleteResponseDto {
    private UUID menuId;
    private LocalDateTime deletedAt;


    public MenuDeleteResponseDto(UUID menuId, LocalDateTime deletedAt) {
        this.menuId = menuId;
        this.deletedAt = deletedAt;
    }

    public static MenuDeleteResponseDto from(Menu menu) {
        return new MenuDeleteResponseDto(menu.getId(), menu.getDeletedAt());
    }
}
