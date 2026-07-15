package com.found404.delivery.domain.store.controller;

import com.found404.delivery.domain.store.dto.request.StoreStatusRequestDto;
import com.found404.delivery.domain.store.dto.response.StorePendingResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreStatusResponseDto;
import com.found404.delivery.domain.store.service.StoreService;
import com.found404.delivery.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class StoreMasterController {

    private final StoreService storeService;

    // 가게 승인 대기 목록?
    @GetMapping ("/stores/approval")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public Slice<StorePendingResponseDto> getPendingStores (
            @PageableDefault(size = 20 , sort = "createdAt", direction = Sort.Direction.DESC)Pageable pageable
    ) {

        return storeService.getPendingStores(pageable);
    }


    // 가게 승인
    @PatchMapping("/stores/{storeId}/approve")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public StoreStatusResponseDto storeApproval (
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId) {
        return storeService.storeApproval(userDetails.getUserId(), storeId);
    }


    // 가게 상태변경
    @PatchMapping("/stores/{storeId}/status")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public StoreStatusResponseDto updateStoreStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId,
            @RequestBody StoreStatusRequestDto request){
        return storeService.updateStoreStatusByMaster(userDetails.getUserId(),storeId, request);
    }

    // 가게 삭제
    @DeleteMapping("/stores/{storeId}")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public StoreStatusResponseDto deleteStore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId
    ){
        return storeService.deleteStoreByMaster(
                userDetails.getUserId(),
                storeId
        );
    }


}
