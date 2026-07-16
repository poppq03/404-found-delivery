package com.found404.delivery.domain.cartitem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "장바구니 수량 변경 요청")
public class CartItemQuantityRequestDto {

    @Schema(description = "변경할 수량 (1 이상)", example = "3")
    @NotNull(message = "메뉴 수량은 1개 이상이어야 합니다.")
    private Integer quantity;
}