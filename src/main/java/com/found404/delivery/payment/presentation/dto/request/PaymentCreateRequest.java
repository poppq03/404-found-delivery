package com.found404.delivery.payment.presentation.dto.request;

import com.found404.delivery.payment.domain.entity.PaymentMethod;
import java.util.UUID;

public record PaymentCreateRequest(
        UUID orderId,
        PaymentMethod paymentMethod
) {
}
