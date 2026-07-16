package com.found404.delivery.domain.order.dto;

import com.found404.delivery.domain.orderitem.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.UUID;

@Schema(description = "주문 상품 응답 DTO")
@Getter
public class OrderItemResponseDto {
    @Schema(description = "주문 상품 ID", example = "11111111-1111-1111-1111-111111111111")
    private final UUID orderItemId;
    @Schema(description = "메뉴 ID", example = "22222222-2222-2222-2222-222222222222")
    private final UUID menuId;
    @Schema(description = "주문 당시 메뉴명", example = "테스트 메뉴")
    private final String menuName;
    @Schema(description = "주문 당시 메뉴 가격", example = "12000")
    private final int menuPrice;
    @Schema(description = "주문 수량", example = "2")
    private final int quantity;
    @Schema(description = "주문 상품 총 금액", example = "24000")
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
