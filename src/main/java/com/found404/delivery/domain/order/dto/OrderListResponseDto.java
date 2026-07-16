package com.found404.delivery.domain.order.dto;

import com.found404.delivery.domain.order.entity.Order;
import com.found404.delivery.domain.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;
import java.time.LocalDateTime;

@Schema(description = "주문 목록 응답 DTO")
@Getter
public class OrderListResponseDto {
    @Schema(description = "주문 ID", example = "11111111-1111-1111-1111-111111111111")
    private final UUID orderId;
    @Schema(description = "가게 ID", example = "22222222-2222-2222-2222-222222222222")
    private final UUID storeId;
    @Schema(description = "주문 상태", example = "REQUESTED")
    private final OrderStatus status;
    @Schema(description = "최종 결제 금액", example = "27000")
    private final int totalPrice;
    @Schema(description = "주문 생성 일시", example = "2026-07-16T10:00:00")
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
