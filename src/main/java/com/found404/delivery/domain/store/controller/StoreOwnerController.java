package com.found404.delivery.domain.store.controller;

import com.found404.delivery.domain.store.dto.request.*;
import com.found404.delivery.domain.store.dto.response.StoreDetailResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreImageUpdateResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreStatusResponseDto;
import com.found404.delivery.domain.store.service.StoreService;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner")
public class StoreOwnerController {

    private final StoreService storeService;

    // 본인 가게 검색

    // 가게 등록
    @PostMapping("/stores")
    @PreAuthorize("hasRole('OWNER')")
    public StoreDetailResponseDto createStore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(value = "image", required=false) MultipartFile image,
            @Valid @RequestPart (value = "request") StoreCreateRequestDto request
    ){
        return storeService.createStore(userDetails.getUserId(),request,image);
    }

    // 가게 수정
    @PatchMapping("/stores/{storeId}")
    public StoreDetailResponseDto updateStore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId,
            @RequestPart(value = "image", required=false) MultipartFile image,
            @Valid @RequestPart(value = "request") StoreUpdateRequestDto request){
        // User 정보 / storeId
        // 가게가 user의 소유인지 확인 | 권한이 owner인지 확인
        // 수정
        return storeService.updateStore(userDetails.getUserId(),storeId,image,request);
        // 가게 수정 후 detail page 로드
    }

    // 가게 삭제
    @DeleteMapping("/stores/{storeId}")
    public StoreStatusResponseDto deleteStore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId){
        return storeService.deleteStore(userDetails.getUserId(),storeId);
    }

    //--------------------------------------------------------------------------------------//

    // 영업상태 변경
    @PatchMapping("/stores/{storeId}/status")
    public StoreStatusResponseDto updateStoreStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId,
            @RequestBody StoreStatusRequestDto request
    ){
        return storeService.updateStoreStatus(userDetails.getUserId(),storeId,request);
    }

    // 최소 주문 금액 수정
    @PatchMapping("/stores/{storeId}/minimumOrderPrice")
    public StoreStatusResponseDto updateMinOrderPrice(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID storeId,
            @RequestBody MinOrderPriceUpdateRequestDto request){
        return storeService.updateMinOrderPrice(userDetails.getUserId(),storeId,request);
    }



}
