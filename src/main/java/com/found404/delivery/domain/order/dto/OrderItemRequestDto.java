package com.found404.delivery.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderItemRequestDto {

    @NotNull(message = "메뉴 ID는 필수 입니다.")
    private UUID menuId;

    @Positive(message = "수량은 1개 이상이어야 합니다.")
    private int quantity;
}
