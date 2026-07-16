package com.found404.delivery.domain.order.controller;

import com.found404.delivery.domain.order.dto.OrderListResponseDto;
import com.found404.delivery.domain.order.dto.OrderResponseDto;
import com.found404.delivery.domain.order.dto.OrderStatusUpdateRequestDto;
import com.found404.delivery.domain.order.service.OrderService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin Order", description = "관리자 주문 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "관리자 - 전체 주문 목록 조회", description = "관리자가 전체 주문 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderListResponseDto>>> getAllOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기. 10, 30, 50만 허용", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
       return ResponseEntity.ok(ApiResponse.success(
               orderService.getAllOrders(userDetails.getRole(), page, size)
       ));
    }

    @Operation(summary = "관리자 - 주문 단건 조회", description = "관리자가 선택한 주문을 조회합니다.")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getAdminOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "주문 ID", required = true)
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getAdminOrder(userDetails.getRole(), orderId)
        ));
    }

    @Operation(summary = "관리자 - 주문 상태 강제 변경", description = "관리자가 주문 상태를 강제로 변경합니다.")
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> changeAdminOrderStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "주문 ID", required = true)
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.changeAdminOrderStatus(userDetails.getRole(), orderId, request),
                "주문 상태가 변경되었습니다."
        ));
    }
}
