package com.found404.delivery.domain.payment.controller;

import com.found404.delivery.domain.payment.dto.request.PaymentCreateRequest;
import com.found404.delivery.domain.payment.dto.response.PaymentResponse;
import com.found404.delivery.domain.payment.service.PaymentService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 생성
     */
    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PaymentCreateRequest request
    ) {
        PaymentResponse response = paymentService.createPayment(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 결제 단건 조회
     */
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID paymentId
    ) {
        PaymentResponse response = paymentService.getPayment(
                userDetails.getUserId(),
                paymentId
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 주문 ID 기준 결제 조회
     */
    @GetMapping("/orders/{orderId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID orderId
    ) {
        PaymentResponse response = paymentService.getPaymentByOrderId(
                userDetails.getUserId(),
                orderId
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 결제 취소
     */
    @PatchMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID paymentId
    ) {
        PaymentResponse response = paymentService.cancelPayment(
                userDetails.getUserId(),
                paymentId
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}