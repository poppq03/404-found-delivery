package com.found404.delivery.domain.payment.dto.request;

import com.found404.delivery.domain.payment.entity.PaymentMethod;

import java.util.UUID;

public record PaymentCreateRequest(
        UUID orderId,
        PaymentMethod paymentMethod
) {
}