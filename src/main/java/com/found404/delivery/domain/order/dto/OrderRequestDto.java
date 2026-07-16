package com.found404.delivery.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Schema(description = "주문 생성 요청 DTO")
@Getter
@NoArgsConstructor
public class OrderRequestDto {

    @Schema(description = "가게 ID", example = "11111111-1111-1111-1111-111111111111")
    @NotNull(message = "가게 ID는 필수입니다.")
    private UUID storeId;

    @Schema(description = "배송지 ID", example = "22222222-2222-2222-2222-222222222222")
    @NotNull(message = "배송지 ID는 필수입니다.")
    private UUID addressId;

    @Schema(description = "배송 요청사항", example = "문 앞에 놓아주세요.")
    @Size(max = 255, message = "배송 요청사항은 255자 이하여야 합니다.")
    private String deliveryRequest;

    @Schema(description = "주문 상품 목록")
    @Valid
    @NotEmpty(message = "주문 상품은 1개 이상이어야 합니다.")
    private List<OrderItemRequestDto> items;
}
