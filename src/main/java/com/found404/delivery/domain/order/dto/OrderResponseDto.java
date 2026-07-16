package com.found404.delivery.domain.order.dto;

import com.found404.delivery.domain.order.entity.Order;
import com.found404.delivery.domain.order.entity.OrderStatus;
import com.found404.delivery.domain.orderitem.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "주문 상세 응답 DTO")
@Getter
public class OrderResponseDto {

    @Schema(description = "주문 ID", example = "11111111-1111-1111-1111-111111111111")
    private final UUID orderId;
    @Schema(description = "가게 ID", example = "22222222-2222-2222-2222-222222222222")
    private final UUID storeId;
    @Schema(description = "배송지 ID", example = "33333333-3333-3333-3333-333333333333")
    private final UUID addressId;
    @Schema(description = "주문 상태", example = "REQUESTED")
    private final OrderStatus status;
    @Schema(description = "메뉴 총 금액", example = "24000")
    private final int totalMenuPrice;
    @Schema(description = "배송비", example = "3000")
    private final int deliveryFee;
    @Schema(description = "할인 금액", example = "0")
    private final int discountPrice;
    @Schema(description = "최종 결제 금액", example = "27000")
    private final int totalPrice;
    @Schema(description = "주문 당시 배송 주소", example = "서울특별시 강남구 테헤란로 123")
    private final String deliveryAddress;
    @Schema(description = "주문 당시 배송 상세 주소", example = "101동 1001호")
    private final String deliveryDetailAddress;
    @Schema(description = "배송 요청사항", example = "문 앞에 놓아주세요.")
    private final String deliveryRequest;
    @Schema(description = "주문 취소 일시", example = "2026-07-16T10:05:00")
    private final LocalDateTime canceledAt;
    @Schema(description = "상태 변경 사유", example = "재료 소진")
    private final String statusReason;
    @Schema(description = "주문 생성 일시", example = "2026-07-16T10:00:00")
    private final LocalDateTime createdAt;
    @Schema(description = "주문 수정 일시", example = "2026-07-16T10:03:00")
    private final LocalDateTime updatedAt;
    @Schema(description = "주문 상품 목록")
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
