package com.found404.delivery.domain.order.dto;

import com.found404.delivery.domain.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderStatusUpdateRequestDto {

    @NotNull
    private OrderStatus status;
}
