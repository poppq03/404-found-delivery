package com.found404.delivery.domain.order.controller;

import com.found404.delivery.domain.order.dto.OrderListResponseDto;
import com.found404.delivery.domain.order.dto.OrderRejectRequestDto;
import com.found404.delivery.domain.order.dto.OrderResponseDto;
import com.found404.delivery.domain.order.dto.OrderStatusUpdateRequestDto;
import com.found404.delivery.domain.order.service.OrderService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner")
public class OwnerOrderController {

    private final OrderService orderService;

    // 내 가게 주문 목록 조회
    @GetMapping("/stores/{storeId}/orders")
    public ResponseEntity<ApiResponse<Page<OrderListResponseDto>>> getMyStoreOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
       return ResponseEntity.ok(ApiResponse.success(orderService.getMyStoreOrders(userDetails.getRole(),storeId, page, size)
       ));
    }

    // 내 가게 주문 단건 조회
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOwnerOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOwnerOrder(userDetails.getRole(), orderId)
        ));
    }

    @PatchMapping("/orders/{orderId}/accept")
    public ResponseEntity<ApiResponse<OrderResponseDto>> acceptOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.acceptOrder(userDetails.getRole(), orderId),
                "주문이 수락되었습니다."
        ));
    }
