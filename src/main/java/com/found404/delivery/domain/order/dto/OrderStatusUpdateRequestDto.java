package com.found404.delivery.domain.order.dto;

import com.found404.delivery.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "주문 상태 변경 요청 DTO")
@Getter
@NoArgsConstructor
public class OrderStatusUpdateRequestDto {

    @Schema(description = "변경할 주문 상태", example = "COOKING")
    @NotNull
    private OrderStatus status;
}
