package com.found404.delivery.domain.payment.controller;

import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.domain.payment.service.PaymentService;
import com.found404.delivery.domain.payment.dto.request.PaymentCreateRequest;
import com.found404.delivery.domain.payment.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @RequestBody PaymentCreateRequest request
    ) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable UUID paymentId
    ) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/orders/{orderId}/payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @PathVariable UUID orderId
    ) {
        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable UUID paymentId
    ) {
        PaymentResponse response = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}