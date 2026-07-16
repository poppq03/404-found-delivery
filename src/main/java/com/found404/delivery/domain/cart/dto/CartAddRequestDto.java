package com.found404.delivery.domain.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "장바구니 담기 요청")
public class CartAddRequestDto {

    @Schema(description = "담을 메뉴 ID", example = "c22d3e4f-5a6b-7c8d-9e0f-1a2b3c4d5e6f")
    @NotNull(message = "메뉴명은 필수입니다.")
    private UUID menuId;

    @Schema(description = "수량 (1 이상)", example = "2")
    @NotNull(message = "메뉴 수량은 1개 이상이어야 합니다.")
    private Integer quantity;
}