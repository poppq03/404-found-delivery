package com.found404.delivery.domain.payment.service;

import com.found404.delivery.domain.payment.entity.Payment;
import com.found404.delivery.domain.payment.entity.PaymentMethod;
import com.found404.delivery.domain.payment.repository.PaymentRepository;
import com.found404.delivery.domain.payment.dto.request.PaymentCreateRequest;
import com.found404.delivery.domain.payment.dto.response.PaymentResponse;
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

        // TODO: Order 도메인 연결 후 orderId로 주문을 조회하고, 주문 최종 금액을 가져오도록 수정
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
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        return PaymentResponse.from(payment);
    }

    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문의 결제 정보를 찾을 수 없습니다."));

        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.cancel();

        return PaymentResponse.from(payment);
    }

    private void validatePaymentMethod(PaymentMethod paymentMethod) {
        if (paymentMethod != PaymentMethod.CARD) {
            // TODO: 팀 공통 ErrorCode 확정 후 CustomException(ErrorCode.INVALID_PAYMENT_METHOD)로 교체
            throw new IllegalArgumentException("CARD 결제 수단만 허용됩니다.");
        }
    }

    private void validateAlreadyPaidOrder(PaymentCreateRequest request) {
        if (paymentRepository.existsByOrderId(request.orderId())) {
            // TODO: 팀 공통 ErrorCode 확정 후 CustomException(ErrorCode.ALREADY_PAID_ORDER)로 교체
            throw new IllegalArgumentException("이미 결제된 주문입니다.");
        }
    }
}