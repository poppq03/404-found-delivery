package com.found404.delivery.domain.payment.dto.request;

import com.found404.delivery.domain.payment.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "결제 생성 요청")
public record PaymentCreateRequest(
        @Schema(description = "결제 대상 주문 ID", example = "b1c2d3e4-5f6a-7b8c-9d0e-1f2a3b4c5d6e")
        UUID orderId,

        @Schema(description = "결제 수단 (CARD만 지원)", example = "CARD")
        PaymentMethod paymentMethod
) {
}