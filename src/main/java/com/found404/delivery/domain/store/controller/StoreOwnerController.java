package com.found404.delivery.domain.store.controller;

import com.found404.delivery.domain.store.dto.request.MinOrderPriceUpdateRequestDto;
import com.found404.delivery.domain.store.dto.request.StoreCreateRequestDto;
import com.found404.delivery.domain.store.dto.request.StoreStatusRequestDto;
import com.found404.delivery.domain.store.dto.request.StoreUpdateRequestDto;
import com.found404.delivery.domain.store.dto.response.StoreDetailResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreStatusResponseDto;
import com.found404.delivery.domain.store.service.StoreService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(
        name = "Store - OWNER",
        description = "OWNER 가게 관리 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner")
@PreAuthorize("hasRole('OWNER')")
public class StoreOwnerController {

    private final StoreService storeService;

    // 가게 등록
    @Operation(
            summary = "가게 등록",
            description = "POST /api/v1/owner/stores"
    )
    @PostMapping(value = "/stores", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StoreDetailResponseDto> createStore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Valid @RequestPart("request") StoreCreateRequestDto request
    ) {
        StoreDetailResponseDto response = storeService.createStore(
                userDetails.getUserId(),
                request,
                image
        );

        return ApiResponse.success(response, "가게 등록 요청이 완료되었습니다.");
    }

    // 가게 수정
    @Operation(
            summary = "가게 수정",
            description = "PATCH /api/v1/owner/stores/{storeId}"
    )
    @PatchMapping(value = "/stores/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StoreDetailResponseDto> updateStore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Valid @RequestPart("request") StoreUpdateRequestDto request
    ) {
        StoreDetailResponseDto response = storeService.updateStore(
                userDetails.getUserId(),
                storeId,
                image,
                request
        );

        return ApiResponse.success(response, "가게 정보가 수정되었습니다.");
    }

    // 가게 삭제
    @Operation(
            summary = "가게 삭제",
            description = "DELETE /api/v1/owner/stores/{storeId}"
    )
    @DeleteMapping("/stores/{storeId}")
    public ApiResponse<StoreStatusResponseDto> deleteStore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId
    ) {
        StoreStatusResponseDto response = storeService.deleteStore(
                userDetails.getUserId(),
                storeId
        );

        return ApiResponse.success(response, "가게가 삭제되었습니다.");
    }

    // 영업 상태 변경
    @Operation(
            summary = "영업 상태 변경",
            description = "PATCH /api/v1/owner/stores/{storeId}/status"
    )
    @PatchMapping("/stores/{storeId}/status")
    public ApiResponse<StoreStatusResponseDto> updateStoreStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreStatusRequestDto request
    ) {
        StoreStatusResponseDto response = storeService.updateStoreStatus(
                userDetails.getUserId(),
                storeId,
                request
        );

        return ApiResponse.success(response, "영업 상태가 변경되었습니다.");
    }

    // 최소 주문 금액 수정
    @Operation(
            summary = "최소 주문 금액 수정",
            description = "PATCH /api/v1/owner/stores/{storeId}/minimum-order-price"
    )
    @PatchMapping("/stores/{storeId}/minimum-order-price")
    public ApiResponse<StoreStatusResponseDto> updateMinOrderPrice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId,
            @Valid @RequestBody MinOrderPriceUpdateRequestDto request
    ) {
        StoreStatusResponseDto response = storeService.updateMinOrderPrice(
                userDetails.getUserId(),
                storeId,
                request
        );

        return ApiResponse.success(response, "최소 주문 금액이 수정되었습니다.");
    }
}