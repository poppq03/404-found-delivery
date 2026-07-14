package com.found404.delivery.domain.cart.controller;

import com.found404.delivery.domain.cart.dto.CartAddRequestDto;
import com.found404.delivery.domain.cart.dto.CartResponseDto;
import com.found404.delivery.domain.cart.service.CartService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart")
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CartResponseDto response = cartService.getCart(
                userDetails.getUserId(), userDetails.getRole());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<ApiResponse<CartResponseDto>> addItem(
            @Valid @RequestBody CartAddRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        CartResponseDto response = cartService.addItem(
                userDetails.getUserId(), userDetails.getRole(), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
