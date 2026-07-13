package com.found404.delivery.domain.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class OrderRequestDto {

    @NotNull(message = "가게 ID는 필수입니다.")
    private UUID storeId;

    @NotNull(message = "배송지 ID는 필수입니다.")
    private UUID addressId;

    @PositiveOrZero(message = "총 메뉴 금액은 0원 이상이어야 합니다.")
    private int totalMenuPrice;

    @PositiveOrZero(message = "배달비는 0원 이상이어야 합니다.")
    private int deliveryFee;

    @PositiveOrZero(message = "할인 금액은 0원 이상이어야 합니다.")
    private int discountPrice;

    @PositiveOrZero(message = "최종 결제 금액은 0원 이상이어야 합니다.")
    private int totalPrice;

    @NotBlank(message = "배송 주소는 필수입니다.")
    @Size(max = 255, message = "배송 주소는 255자 이어야 합니다.")
    private String deliveryAddress;

    @Size(max = 255, message = "상세 배송 주소는 255자 이하여야 합니다.")
    private String deliveryDetailAddress;

    @Size(max = 255, message = "배송 요청사항은 255자 이하여야 합니다.")
    private String deliveryRequest;


}
