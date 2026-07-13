package com.found404.delivery.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderRequestDto {

    @NotNull(message = "가게 ID는 필수입니다.")
    private UUID storeId;

    @NotNull(message = "배송지 ID는 필수입니다.")
    private UUID addressId;

    @Size(max = 255, message = "배송 요청사항은 255자 이하여야 합니다.")
    private String deliveryRequest;

    @Valid
    @NotEmpty(message = "주문 상품은 1개 이상이어야 합니다.")
    private List<OrderItemRequestDto> items;
}
