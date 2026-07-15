package com.found404.delivery.domain.payment.service;

import com.found404.delivery.domain.payment.dto.request.PaymentCreateRequest;
import com.found404.delivery.domain.payment.dto.response.PaymentResponse;
import com.found404.delivery.domain.payment.entity.Payment;
import com.found404.delivery.domain.payment.entity.PaymentMethod;
import com.found404.delivery.domain.payment.entity.PaymentStatus;
import com.found404.delivery.domain.payment.repository.PaymentRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request) {
        validatePaymentMethod(request.paymentMethod());
        validateAlreadyPaidOrder(request);

        // TODO: 로그인/인증 기능 완성 후 JWT에서 로그인 사용자 ID를 가져오도록 수정
        Long userId = 1L;

        // TODO: Order 도메인 연결 후 orderId로 주문을 조회하고,
        // 주문 최종 금액을 가져오도록 수정
        Integer amount = 18000;

        Payment payment = Payment.create(
                request.orderId(),
                userId,
                request.paymentMethod(),
                amount
        );

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.from(savedPayment);
    }

    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = findPayment(paymentId);

        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.PAYMENT_NOT_FOUND)
                );

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId) {
        Payment payment = findPayment(paymentId);

        validateCancelablePayment(payment);

        payment.cancel();

        return PaymentResponse.from(payment);
    }

    private Payment findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.PAYMENT_NOT_FOUND)
                );
    }

    private void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod == null || paymentMethod != PaymentMethod.CARD) {
            throw new CustomException(
                    ErrorCode.INVALID_PAYMENT_METHOD
            );
        }
    }

    private void validateAlreadyPaidOrder(PaymentCreateRequest request) {
        if (paymentRepository.existsByOrderId(request.orderId())) {
            throw new CustomException(
                    ErrorCode.ALREADY_PAID_ORDER
            );
        }
    }

    private void validateCancelablePayment(Payment payment) {
        if (payment.getPaymentStatus() == PaymentStatus.CANCELED) {
            throw new CustomException(
                    ErrorCode.ALREADY_CANCELED_PAYMENT
            );
        }

        if (payment.getPaymentStatus() != PaymentStatus.PAID) {
            throw new CustomException(
                    ErrorCode.INVALID_PAYMENT_STATUS
            );
        }
    }
}