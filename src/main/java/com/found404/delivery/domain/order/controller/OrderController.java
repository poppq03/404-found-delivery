package com.found404.delivery.domain.order.controller;

import com.found404.delivery.domain.order.dto.OrderListResponseDto;
import com.found404.delivery.domain.order.dto.OrderRequestDto;
import com.found404.delivery.domain.order.dto.OrderResponseDto;
import com.found404.delivery.domain.order.service.OrderService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Order", description = "고객 주문 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "고객이 가게의 메뉴를 선택해 주문을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDto>> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderRequestDto request
    ) {
        OrderResponseDto response = orderService.createOrder(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "주문이 생성되었습니다."));
    }

    @Operation(summary = "내 주문 목록 조회", description = "로그인한 고객의 주문 목록을 페이지 단위로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderListResponseDto>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기. 10, 30, 50만 허용", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<OrderListResponseDto> response = orderService.getMyOrders(
                userDetails.getUserId(),
                page,
                size
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "내 주문 단건 조회", description = "로그인한 고객이 본인의 주문 상세 정보를 조회합니다.")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getMyOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "주문 ID", required = true)
            @PathVariable UUID orderId
    ) {
        OrderResponseDto response = orderService.getMyOrder(
                userDetails.getUserId(),
                orderId
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "주문 취소", description = "주문 생성 후 5분 이내의 REQUESTED 상태 주문을 취소합니다.")
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponseDto>> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "주문 ID", required = true)
            @PathVariable UUID orderId
    ) {
        OrderResponseDto response = orderService.cancelOrder(
                userDetails.getUserId(),
                orderId
        );

        return ResponseEntity.ok(ApiResponse.success(response, "주문이 취소되었습니다."));
    }
}
