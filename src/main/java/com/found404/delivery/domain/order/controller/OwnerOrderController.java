package com.found404.delivery.domain.order.controller;

import com.found404.delivery.domain.order.dto.OrderListResponseDto;
import com.found404.delivery.domain.order.dto.OrderRejectRequestDto;
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

@Tag(name = "Owner Order", description = "사장님 주문 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner")
public class OwnerOrderController {

    private final OrderService orderService;

    // 내 가게 주문 목록 조회
    @Operation(summary = "내 가게 주문 목록 조회", description = "사장님이 본인 소유 가게의 주문 목록을 조회합니다.")
    @GetMapping("/stores/{storeId}/orders")
    public ResponseEntity<ApiResponse<Page<OrderListResponseDto>>> getMyStoreOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "가게 ID", required = true)
            @PathVariable UUID storeId,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기. 10, 30, 50만 허용", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
       return ResponseEntity.ok(ApiResponse.success(orderService
               .getMyStoreOrders(
                       userDetails.getUserId(),
                       userDetails.getRole(),
                       storeId,
                       page,
                       size
               )
       ));
    }

    // 내 가게 주문 단건 조회
    @Operation(summary = "내 가게 주문 단건 조회", description = "사장님이 본인 소유 가게의 선택한 주문을 조회합니다.")
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOwnerOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "주문 ID", required = true)
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOwnerOrder(userDetails.getUserId(), userDetails.getRole(), orderId)
        ));
    }

    @Operation(summary = "주문 수락", description = "사장님이 REQUESTED 상태의 주문을 수락합니다.")
    @PatchMapping("/orders/{orderId}/accept")
    public ResponseEntity<ApiResponse<OrderResponseDto>> acceptOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "주문 ID", required = true)
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.acceptOrder(userDetails.getUserId(), userDetails.getRole(), orderId),
                "주문이 수락되었습니다."
        ));
    }

    @Operation(summary = "주문 거절", description = "사장님이 REQUESTED 상태의 주문을 거절합니다.")
    @PatchMapping("/orders/{orderId}/reject")
    public ResponseEntity<ApiResponse<OrderResponseDto>> rejectOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "주문 ID", required = true)
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderRejectRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.rejectOrder(userDetails.getUserId(), userDetails.getRole(), orderId, request)
                , "주문이 거절 되었습니다."
        ));
    }

    @Operation(summary = "주문 상태 변경", description = "사장님이 주문의 상태를 변경합니다.")
    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> changeOrderStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "주문 ID", required = true)
            @PathVariable UUID orderId,
            @Valid @RequestBody OrderStatusUpdateRequestDto request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.changeOwnerOrderStatus(userDetails.getUserId(), userDetails.getRole(), orderId, request),
                "주문 상태가 변경되었습니다."
        ));
    }
}
