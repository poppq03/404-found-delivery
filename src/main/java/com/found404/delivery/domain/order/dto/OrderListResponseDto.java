package com.found404.delivery.domain.order.dto;

import com.found404.delivery.domain.order.entity.Order;
import com.found404.delivery.domain.order.entity.OrderStatus;
import lombok.Getter;

import java.util.UUID;
import java.time.LocalDateTime;

@Getter
public class OrderListResponseDto {
    private final UUID orderId;
    private final UUID storeId;
    private final OrderStatus status;
    private final int totalPrice;
    private final LocalDateTime createdAt;

    public OrderListResponseDto(
            UUID orderId,
            UUID storeId,
            OrderStatus status,
            int totalPrice,
            LocalDateTime createdAt
    ) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.status = status;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    public static OrderListResponseDto from(Order order) {
        return new OrderListResponseDto(
                order.getId(),
                order.getStoreId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getCreatedAt()
        );
    }
}
