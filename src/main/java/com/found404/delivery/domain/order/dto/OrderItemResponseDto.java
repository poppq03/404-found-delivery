package com.found404.delivery.domain.order.dto;

import com.found404.delivery.domain.orderItem.entity.OrderItem;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OrderItemResponseDto {
    private final UUID orderItemId;
    private final UUID menuId;
    private final String menuName;
    private final int menuPrice;
    private final int quantity;
    private final int totalPrice;

    public OrderItemResponseDto(UUID orderItemId, UUID menuId, String menuName, int menuPrice, int quantity, int totalPrice) {
        this.orderItemId = orderItemId;
        this.menuId = menuId;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }

    public static OrderItemResponseDto from(OrderItem orderItem) {
        return new OrderItemResponseDto(
                orderItem.getId(),
                orderItem.getMenuId(),
                orderItem.getMenuName(),
                orderItem.getMenuPrice(),
                orderItem.getQuantity(),
                orderItem.getTotalPrice()
        );
    }
}