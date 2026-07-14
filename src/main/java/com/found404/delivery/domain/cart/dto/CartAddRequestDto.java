package com.found404.delivery.domain.cart.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CartAddRequestDto {

    @NotNull(message = "메뉴명은 필수입니다.")
    private UUID menuId;

    @NotNull(message = "메뉴 수량은 1개 이상이어야 합니다.")
    private Integer quantity;
}
