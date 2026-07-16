package com.found404.delivery.domain.payment.controller;

import com.found404.delivery.domain.payment.dto.request.PaymentCreateRequest;
import com.found404.delivery.domain.payment.dto.response.PaymentResponse;
import com.found404.delivery.domain.payment.service.PaymentService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Payment", description = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 생성
     */
    @Operation(summary = "결제 생성", description = "주문자 본인이 결제를 생성합니다. (결제 수단 CARD만, 금액은 주문 총액 기준, 주문 1건당 결제 1건)")
    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
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
    @Operation(summary = "결제 단건 조회", description = "결제 ID로 본인의 결제 내역을 조회합니다.")
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "결제 ID") @PathVariable UUID paymentId
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
    @Operation(summary = "주문별 결제 조회", description = "주문 ID로 해당 주문의 본인 결제 내역을 조회합니다.")
    @GetMapping("/orders/{orderId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "주문 ID") @PathVariable UUID orderId
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
    @Operation(summary = "결제 취소", description = "결제 완료(PAID) 상태인 본인 결제를 취소합니다.")
    @PatchMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "결제 ID") @PathVariable UUID paymentId
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