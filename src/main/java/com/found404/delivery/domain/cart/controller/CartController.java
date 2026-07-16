package com.found404.delivery.domain.cart.controller;

import com.found404.delivery.domain.cart.dto.CartAddRequestDto;
import com.found404.delivery.domain.cart.dto.CartResponseDto;
import com.found404.delivery.domain.cart.service.CartService;
import com.found404.delivery.domain.cartitem.dto.CartItemQuantityRequestDto;
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

@Tag(name = "Cart", description = "장바구니 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니 조회", description = "로그인한 고객의 장바구니를 조회합니다.")
    @GetMapping("/cart")
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        CartResponseDto response = cartService.getCart(
                userDetails.getUserId(), userDetails.getRole());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "장바구니 담기", description = "메뉴를 장바구니에 담습니다. 다른 가게 메뉴면 기존 항목을 비우고 교체합니다.")
    @PostMapping("/cart/items")
    public ResponseEntity<ApiResponse<CartResponseDto>> addItem(
            @Valid @RequestBody CartAddRequestDto request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        CartResponseDto response = cartService.addItem(
                userDetails.getUserId(), userDetails.getRole(), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "장바구니 수량 변경", description = "장바구니 항목의 수량을 변경합니다.")
    @PatchMapping("/cart/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> changeQuantity(
            @Parameter(description = "장바구니 항목 ID") @PathVariable UUID cartItemId,
            @Valid @RequestBody CartItemQuantityRequestDto request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        CartResponseDto response = cartService.changeQuantity(
                userDetails.getUserId(), userDetails.getRole(), cartItemId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "장바구니 항목 삭제", description = "장바구니에서 개별 항목을 제거합니다.")
    @DeleteMapping("/cart/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> removeItem(
            @Parameter(description = "장바구니 항목 ID") @PathVariable UUID cartItemId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        CartResponseDto response = cartService.removeItem(
                userDetails.getUserId(), userDetails.getRole(), cartItemId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "장바구니 전체 비우기", description = "장바구니의 모든 항목을 제거합니다.")
    @DeleteMapping("/cart")
    public ResponseEntity<ApiResponse<CartResponseDto>> clearCart(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        CartResponseDto response = cartService.clearCart(
                userDetails.getUserId(), userDetails.getRole());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}