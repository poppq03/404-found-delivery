package com.found404.delivery.domain.payment.service;

import com.found404.delivery.domain.order.entity.Order;
import com.found404.delivery.domain.order.repository.OrderRepository;
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

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    /**
     * 결제 생성
     */
    @Transactional
    public PaymentResponse createPayment(
            Long userId,
            PaymentCreateRequest request
    ) {
        validatePaymentMethod(request.paymentMethod());

        // 실제 존재하는 주문인지 확인
        Order order = findOrder(request.orderId());

        // 로그인 사용자가 해당 주문을 한 본인인지 확인
        validateOrderOwner(order, userId);

        // 해당 주문에 이미 생성된 결제가 있는지 확인
        validateAlreadyPaidOrder(order.getId());

        // 클라이언트 요청값이 아닌 실제 주문의 최종 결제 금액 사용
        Integer amount = order.getTotalPrice();

        Payment payment = Payment.create(
                order.getId(),
                userId,
                request.paymentMethod(),
                amount
        );

        Payment savedPayment = paymentRepository.save(payment);

        return PaymentResponse.from(savedPayment);
    }

    /**
     * 결제 단건 조회
     */
    public PaymentResponse getPayment(
            Long userId,
            UUID paymentId
    ) {
        Payment payment = findPayment(paymentId);

        // 로그인 사용자의 결제인지 확인
        validatePaymentOwner(payment, userId);

        return PaymentResponse.from(payment);
    }

    /**
     * 주문 ID 기준 결제 조회
     */
    public PaymentResponse getPaymentByOrderId(
            Long userId,
            UUID orderId
    ) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.PAYMENT_NOT_FOUND
                        )
                );

        // 로그인 사용자의 결제인지 확인
        validatePaymentOwner(payment, userId);

        return PaymentResponse.from(payment);
    }

    /**
     * 결제 취소
     */
    @Transactional
    public PaymentResponse cancelPayment(
            Long userId,
            UUID paymentId
    ) {
        Payment payment = findPayment(paymentId);

        // 로그인 사용자의 결제인지 확인
        validatePaymentOwner(payment, userId);

        // 취소 가능한 결제 상태인지 확인
        validateCancelablePayment(payment);

        payment.cancel();

        return PaymentResponse.from(payment);
    }

    /**
     * 결제 조회 공통 메서드
     */
    private Payment findPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.PAYMENT_NOT_FOUND
                        )
                );
    }

    /**
     * 주문 조회 공통 메서드
     */
    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.ORDER_NOT_FOUND
                        )
                );
    }

    /**
     * 결제 수단 검증
     */
    private void validatePaymentMethod(
            PaymentMethod paymentMethod
    ) {
        if (paymentMethod == null
                || paymentMethod != PaymentMethod.CARD) {
            throw new CustomException(
                    ErrorCode.INVALID_PAYMENT_METHOD
            );
        }
    }

    /**
     * 주문별 중복 결제 검증
     */
    private void validateAlreadyPaidOrder(UUID orderId) {
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new CustomException(
                    ErrorCode.ALREADY_PAID_ORDER
            );
        }
    }

    /**
     * 로그인 사용자가 주문한 본인인지 검증
     */
    private void validateOrderOwner(
            Order order,
            Long userId
    ) {
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN
            );
        }
    }

    /**
     * 로그인 사용자가 결제한 본인인지 검증
     */
    private void validatePaymentOwner(
            Payment payment,
            Long userId
    ) {
        if (!Objects.equals(payment.getUserId(), userId)) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN
            );
        }
    }

    /**
     * 결제 취소 가능 상태 검증
     */
    private void validateCancelablePayment(Payment payment) {
        if (payment.getPaymentStatus()
                == PaymentStatus.CANCELED) {
            throw new CustomException(
                    ErrorCode.ALREADY_CANCELED_PAYMENT
            );
        }

        if (payment.getPaymentStatus()
                != PaymentStatus.PAID) {
            throw new CustomException(
                    ErrorCode.INVALID_PAYMENT_STATUS
            );
        }
    }
}