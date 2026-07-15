package com.found404.delivery.domain.order.dto;

import com.found404.delivery.domain.order.entity.Order;
import com.found404.delivery.domain.order.entity.OrderStatus;
import com.found404.delivery.domain.orderitem.entity.OrderItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class OrderResponseDto {

    private final UUID orderId;
    private final UUID storeId;
    private final UUID addressId;
    private final OrderStatus status;
    private final int totalMenuPrice;
    private final int deliveryFee;
    private final int discountPrice;
    private final int totalPrice;
    private final String deliveryAddress;
    private final String deliveryDetailAddress;
    private final String deliveryRequest;
    private final LocalDateTime canceledAt;
    private final String statusReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final List<OrderItemResponseDto> items;


    public OrderResponseDto(
            UUID orderId,
            UUID storeId,
            UUID addressId,
            OrderStatus status,
            int totalMenuPrice,
            int deliveryFee,
            int discountPrice,
            int totalPrice,
            String deliveryAddress,
            String deliveryDetailAddress,
            String deliveryRequest,
            LocalDateTime canceledAt,
            String statusReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<OrderItemResponseDto> items
    ) {
        this.orderId = orderId;
        this.storeId = storeId;
        this.addressId = addressId;
        this.status = status;
        this.totalMenuPrice = totalMenuPrice;
        this.deliveryFee = deliveryFee;
        this.discountPrice = discountPrice;
        this.totalPrice = totalPrice;
        this.deliveryAddress = deliveryAddress;
        this.deliveryDetailAddress = deliveryDetailAddress;
        this.deliveryRequest = deliveryRequest;
        this.canceledAt = canceledAt;
        this.statusReason = statusReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
    }

    public static OrderResponseDto from(Order order, List<OrderItem> orderItems) {
        return new OrderResponseDto(
                order.getId(),
                order.getStoreId(),
                order.getAddressId(),
                order.getStatus(),
                order.getTotalMenuPrice(),
                order.getDeliveryFee(),
                order.getDiscountPrice(),
                order.getTotalPrice(),
                order.getDeliveryAddress(),
                order.getDeliveryDetailAddress(),
                order.getDeliveryRequest(),
                order.getCanceledAt(),
                order.getStatusReason(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                orderItems.stream()
                        .map(OrderItemResponseDto::from)
                        .toList()
        );
    }

}
