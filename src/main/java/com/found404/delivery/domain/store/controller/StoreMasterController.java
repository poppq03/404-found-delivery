package com.found404.delivery.domain.store.controller;

import com.found404.delivery.domain.store.dto.request.StoreStatusRequestDto;
import com.found404.delivery.domain.store.dto.response.StorePendingResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreStatusResponseDto;
import com.found404.delivery.domain.store.service.StoreService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Store - MASTER | MANAGER",
        description = "MASTER | MANAGER 가게 관리 API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class StoreMasterController {

    private final StoreService storeService;

    // 가게 승인 대기 목록 조회
    @Operation(
            summary = "가게 승인 대기 목록 조회",
            description = "GET /api/v1/admin/stores/approval"
    )
    @GetMapping("/stores/approval")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ApiResponse<Slice<StorePendingResponseDto>> getPendingStores(
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Slice<StorePendingResponseDto> response =
                storeService.getPendingStores(pageable);

        return ApiResponse.success(response);
    }

    // 가게 승인
    @Operation(
            summary = "가게 승인",
            description = "PATCH /api/v1/admin/stores/{storeId}/approve"
    )
    @PatchMapping("/stores/{storeId}/approve")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ApiResponse<StoreStatusResponseDto> storeApproval(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId
    ) {
        StoreStatusResponseDto response =
                storeService.storeApproval(
                        userDetails.getUserId(),
                        storeId
                );

        return ApiResponse.success(response);
    }

    // 가게 상태 변경
    @Operation(
            summary = "가게 상태 변경",
            description = "PATCH /api/v1/admin/stores/{storeId}/status"
    )
    @PatchMapping("/stores/{storeId}/status")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ApiResponse<StoreStatusResponseDto> updateStoreStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId,
            @RequestBody StoreStatusRequestDto request
    ) {
        StoreStatusResponseDto response =
                storeService.updateStoreStatusByMaster(
                        userDetails.getUserId(),
                        storeId,
                        request
                );

        return ApiResponse.success(response);
    }

    // 가게 삭제
    @Operation(
            summary = "가게 삭제",
            description = "DELETE /api/v1/admin/stores/{storeId}"
    )
    @DeleteMapping("/stores/{storeId}")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ApiResponse<StoreStatusResponseDto> deleteStore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId
    ) {
        StoreStatusResponseDto response =
                storeService.deleteStoreByMaster(
                        userDetails.getUserId(),
                        storeId
                );

        return ApiResponse.success(response);
    }
}