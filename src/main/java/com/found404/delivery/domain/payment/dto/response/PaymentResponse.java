package com.found404.delivery.domain.payment.dto.response;

import com.found404.delivery.domain.payment.entity.Payment;
import com.found404.delivery.domain.payment.entity.PaymentMethod;
import com.found404.delivery.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        Long userId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        Integer amount,
        LocalDateTime paidAt,
        LocalDateTime canceledAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getAmount(),
                payment.getPaidAt(),
                payment.getCanceledAt()
        );
    }
}
