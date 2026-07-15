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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    /**
     * 테스트용 Order 객체 생성
     *
     * Order 엔티티 생성 방식이 복잡할 수 있어서
     * 실제 Order를 만들지 않고 Mockito 가짜 객체를 사용한다.
     */
    private Order mockOrder(
            UUID orderId,
            Long userId,
            Integer totalPrice
    ) {
        Order order = org.mockito.Mockito.mock(Order.class);

        when(order.getId()).thenReturn(orderId);
        when(order.getUserId()).thenReturn(userId);
        when(order.getTotalPrice()).thenReturn(totalPrice);

        return order;
    }

    /**
     * 테스트용 Payment 객체 생성
     */
    private Payment paymentWithId(
            UUID paymentId,
            UUID orderId,
            Long userId,
            PaymentStatus paymentStatus
    ) {
        Payment payment = Payment.create(
                orderId,
                userId,
                PaymentMethod.CARD,
                15000
        );

        ReflectionTestUtils.setField(
                payment,
                "paymentId",
                paymentId
        );

        ReflectionTestUtils.setField(
                payment,
                "paymentStatus",
                paymentStatus
        );

        return payment;
    }

    /**
     * CustomException과 ErrorCode를 반복해서 검증하기 위한 메서드
     */
    private void assertErrorCode(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable action,
            ErrorCode expectedErrorCode
    ) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(
                        error -> ((CustomException) error).getErrorCode()
                )
                .isEqualTo(expectedErrorCode);
    }

    // ===== createPayment =====

    @Test
    @DisplayName("결제 생성 성공 - 주문의 실제 최종 금액으로 결제를 저장한다")
    void createPayment_success() {
        // given
        Long userId = 1L;
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        Integer totalPrice = 18000;

        PaymentCreateRequest request =
                new PaymentCreateRequest(
                        orderId,
                        PaymentMethod.CARD
                );

        Order order = mockOrder(
                orderId,
                userId,
                totalPrice
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(orderId))
                .thenReturn(false);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment payment = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            payment,
                            "paymentId",
                            paymentId
                    );

                    return payment;
                });

        // when
        PaymentResponse response =
                paymentService.createPayment(
                        userId,
                        request
                );

        // then
        assertThat(response.paymentId())
                .isEqualTo(paymentId);

        assertThat(response.orderId())
                .isEqualTo(orderId);

        assertThat(response.userId())
                .isEqualTo(userId);

        assertThat(response.paymentMethod())
                .isEqualTo(PaymentMethod.CARD);

        assertThat(response.paymentStatus())
                .isEqualTo(PaymentStatus.PAID);

        assertThat(response.amount())
                .isEqualTo(totalPrice);

        assertThat(response.paidAt())
                .isNotNull();

        verify(paymentRepository)
                .save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 생성 실패 - 결제 수단이 null이면 INVALID_PAYMENT_METHOD 예외")
    void createPayment_fail_nullPaymentMethod() {
        // given
        UUID orderId = UUID.randomUUID();

        PaymentCreateRequest request =
                new PaymentCreateRequest(
                        orderId,
                        null
                );

        // when & then
        assertErrorCode(
                () -> paymentService.createPayment(
                        1L,
                        request
                ),
                ErrorCode.INVALID_PAYMENT_METHOD
        );

        verify(orderRepository, never())
                .findById(any());

        verify(paymentRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("결제 생성 실패 - 주문이 존재하지 않으면 ORDER_NOT_FOUND 예외")
    void createPayment_fail_orderNotFound() {
        // given
        UUID orderId = UUID.randomUUID();

        PaymentCreateRequest request =
                new PaymentCreateRequest(
                        orderId,
                        PaymentMethod.CARD
                );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // when & then
        assertErrorCode(
                () -> paymentService.createPayment(
                        1L,
                        request
                ),
                ErrorCode.ORDER_NOT_FOUND
        );

        verify(paymentRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("결제 생성 실패 - 본인의 주문이 아니면 FORBIDDEN 예외")
    void createPayment_fail_notOrderOwner() {
        // given
        Long loginUserId = 1L;
        Long orderOwnerId = 2L;
        UUID orderId = UUID.randomUUID();

        PaymentCreateRequest request =
                new PaymentCreateRequest(
                        orderId,
                        PaymentMethod.CARD
                );

        Order order = mockOrder(
                orderId,
                orderOwnerId,
                15000
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // when & then
        assertErrorCode(
                () -> paymentService.createPayment(
                        loginUserId,
                        request
                ),
                ErrorCode.FORBIDDEN
        );

        verify(paymentRepository, never())
                .existsByOrderId(any());

        verify(paymentRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("결제 생성 실패 - 이미 결제된 주문이면 ALREADY_PAID_ORDER 예외")
    void createPayment_fail_alreadyPaidOrder() {
        // given
        Long userId = 1L;
        UUID orderId = UUID.randomUUID();

        PaymentCreateRequest request =
                new PaymentCreateRequest(
                        orderId,
                        PaymentMethod.CARD
                );

        Order order = mockOrder(
                orderId,
                userId,
                15000
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(paymentRepository.existsByOrderId(orderId))
                .thenReturn(true);

        // when & then
        assertErrorCode(
                () -> paymentService.createPayment(
                        userId,
                        request
                ),
                ErrorCode.ALREADY_PAID_ORDER
        );

        verify(paymentRepository, never())
                .save(any());
    }

    // ===== getPayment =====

    @Test
    @DisplayName("결제 단건 조회 성공")
    void getPayment_success() {
        // given
        Long userId = 1L;
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Payment payment = paymentWithId(
                paymentId,
                orderId,
                userId,
                PaymentStatus.PAID
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        // when
        PaymentResponse response =
                paymentService.getPayment(
                        userId,
                        paymentId
                );

        // then
        assertThat(response.paymentId())
                .isEqualTo(paymentId);

        assertThat(response.orderId())
                .isEqualTo(orderId);

        assertThat(response.userId())
                .isEqualTo(userId);

        assertThat(response.paymentStatus())
                .isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("결제 단건 조회 실패 - 결제가 없으면 PAYMENT_NOT_FOUND 예외")
    void getPayment_fail_notFound() {
        // given
        UUID paymentId = UUID.randomUUID();

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        // when & then
        assertErrorCode(
                () -> paymentService.getPayment(
                        1L,
                        paymentId
                ),
                ErrorCode.PAYMENT_NOT_FOUND
        );
    }

    @Test
    @DisplayName("결제 단건 조회 실패 - 결제 소유자가 아니면 FORBIDDEN 예외")
    void getPayment_fail_notPaymentOwner() {
        // given
        UUID paymentId = UUID.randomUUID();

        Payment payment = paymentWithId(
                paymentId,
                UUID.randomUUID(),
                2L,
                PaymentStatus.PAID
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        // when & then
        assertErrorCode(
                () -> paymentService.getPayment(
                        1L,
                        paymentId
                ),
                ErrorCode.FORBIDDEN
        );
    }

    // ===== getPaymentByOrderId =====

    @Test
    @DisplayName("주문 ID 기준 결제 조회 성공")
    void getPaymentByOrderId_success() {
        // given
        Long userId = 1L;
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        Payment payment = paymentWithId(
                paymentId,
                orderId,
                userId,
                PaymentStatus.PAID
        );

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));

        // when
        PaymentResponse response =
                paymentService.getPaymentByOrderId(
                        userId,
                        orderId
                );

        // then
        assertThat(response.paymentId())
                .isEqualTo(paymentId);

        assertThat(response.orderId())
                .isEqualTo(orderId);

        assertThat(response.userId())
                .isEqualTo(userId);
    }

    @Test
    @DisplayName("주문 ID 기준 조회 실패 - 결제가 없으면 PAYMENT_NOT_FOUND 예외")
    void getPaymentByOrderId_fail_notFound() {
        // given
        UUID orderId = UUID.randomUUID();

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        // when & then
        assertErrorCode(
                () -> paymentService.getPaymentByOrderId(
                        1L,
                        orderId
                ),
                ErrorCode.PAYMENT_NOT_FOUND
        );
    }

    @Test
    @DisplayName("주문 ID 기준 조회 실패 - 결제 소유자가 아니면 FORBIDDEN 예외")
    void getPaymentByOrderId_fail_notPaymentOwner() {
        // given
        UUID orderId = UUID.randomUUID();

        Payment payment = paymentWithId(
                UUID.randomUUID(),
                orderId,
                2L,
                PaymentStatus.PAID
        );

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(payment));

        // when & then
        assertErrorCode(
                () -> paymentService.getPaymentByOrderId(
                        1L,
                        orderId
                ),
                ErrorCode.FORBIDDEN
        );
    }

    // ===== cancelPayment =====

    @Test
    @DisplayName("결제 취소 성공 - PAID 상태를 CANCELED로 변경한다")
    void cancelPayment_success() {
        // given
        Long userId = 1L;
        UUID paymentId = UUID.randomUUID();

        Payment payment = paymentWithId(
                paymentId,
                UUID.randomUUID(),
                userId,
                PaymentStatus.PAID
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        // when
        PaymentResponse response =
                paymentService.cancelPayment(
                        userId,
                        paymentId
                );

        // then
        assertThat(response.paymentStatus())
                .isEqualTo(PaymentStatus.CANCELED);

        assertThat(payment.getCanceledAt())
                .isNotNull();
    }

    @Test
    @DisplayName("결제 취소 실패 - 결제가 없으면 PAYMENT_NOT_FOUND 예외")
    void cancelPayment_fail_notFound() {
        // given
        UUID paymentId = UUID.randomUUID();

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());

        // when & then
        assertErrorCode(
                () -> paymentService.cancelPayment(
                        1L,
                        paymentId
                ),
                ErrorCode.PAYMENT_NOT_FOUND
        );
    }

    @Test
    @DisplayName("결제 취소 실패 - 결제 소유자가 아니면 FORBIDDEN 예외")
    void cancelPayment_fail_notPaymentOwner() {
        // given
        UUID paymentId = UUID.randomUUID();

        Payment payment = paymentWithId(
                paymentId,
                UUID.randomUUID(),
                2L,
                PaymentStatus.PAID
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        // when & then
        assertErrorCode(
                () -> paymentService.cancelPayment(
                        1L,
                        paymentId
                ),
                ErrorCode.FORBIDDEN
        );

        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.PAID);
    }

    @Test
    @DisplayName("결제 취소 실패 - 이미 취소된 결제면 ALREADY_CANCELED_PAYMENT 예외")
    void cancelPayment_fail_alreadyCanceled() {
        // given
        Long userId = 1L;
        UUID paymentId = UUID.randomUUID();

        Payment payment = paymentWithId(
                paymentId,
                UUID.randomUUID(),
                userId,
                PaymentStatus.CANCELED
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        // when & then
        assertErrorCode(
                () -> paymentService.cancelPayment(
                        userId,
                        paymentId
                ),
                ErrorCode.ALREADY_CANCELED_PAYMENT
        );
    }

    @Test
    @DisplayName("결제 취소 실패 - FAILED 상태면 INVALID_PAYMENT_STATUS 예외")
    void cancelPayment_fail_invalidPaymentStatus() {
        // given
        Long userId = 1L;
        UUID paymentId = UUID.randomUUID();

        Payment payment = paymentWithId(
                paymentId,
                UUID.randomUUID(),
                userId,
                PaymentStatus.FAILED
        );

        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        // when & then
        assertErrorCode(
                () -> paymentService.cancelPayment(
                        userId,
                        paymentId
                ),
                ErrorCode.INVALID_PAYMENT_STATUS
        );
    }
}