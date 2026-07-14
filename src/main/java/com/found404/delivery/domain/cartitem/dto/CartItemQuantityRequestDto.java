package com.found404.delivery.domain.cartitem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartItemQuantityRequestDto {

    @NotNull(message = "메뉴 수량은 1개 이상이어야 합니다.")
    private Integer quantity;
}
