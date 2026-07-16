package com.found404.delivery.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Schema(description = "주문 상품 요청 DTO")
@Getter
@NoArgsConstructor
public class OrderItemRequestDto {

    @Schema(description = "메뉴 ID", example = "11111111-1111-1111-1111-111111111111")
    @NotNull(message = "메뉴 ID는 필수 입니다.")
    private UUID menuId;

    @Schema(description = "주문 수량", example = "2")
    @Positive(message = "수량은 1개 이상이어야 합니다.")
    private int quantity;
}
