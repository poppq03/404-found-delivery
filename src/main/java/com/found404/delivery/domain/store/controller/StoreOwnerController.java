package com.found404.delivery.domain.store.controller;

import com.found404.delivery.domain.store.dto.request.*;
import com.found404.delivery.domain.store.dto.response.StoreDetailResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreImageUpdateResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreStatusResponseDto;
import com.found404.delivery.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreOwnerController {

    private final StoreService storeService;

    // 본인 가게 검색

    // 가게 등록
    @PostMapping("/stores")
    public StoreDetailResponseDto createStore(@RequestBody StoreCreateRequestDto request){
        return null;
    }

    // 가게 수정
    @PatchMapping("/stores/{storeId}")
    public StoreUpdateRequestDto updateStore(@PathVariable UUID storeId, @RequestBody StoreUpdateRequestDto request){

        return null;
    }

    // 가게 삭제
    @DeleteMapping("/stores/{storeId}")
    public StoreStatusResponseDto deleteStore(){

        return null;
    }

    // 영업상태 변경
    @PatchMapping("/stores/{storeId}/status")
    public StoreStatusResponseDto updateStoreStatus(@PathVariable UUID storeId, @RequestBody StoreStatusRequestDto request){
        return null;
    }

    // 영업 Open
    @PatchMapping("/stores/{storeId}/open")
    public StoreStatusResponseDto openStore(@PathVariable UUID storeId, @RequestBody StoreStatusRequestDto request){
        return null;
    }
    // 영업 Close
    @PatchMapping("/stores/{storeId}/close")
    public StoreStatusResponseDto closeStore(@PathVariable UUID storeId, @RequestBody StoreStatusRequestDto request){
        return null;
    }

    // 최소 주문 금액 수정
    @PatchMapping("/stores/{storeId}/minimumOrderPrice")
    public StoreDetailResponseDto updateMinOrderPrice(@PathVariable UUID storeId, @RequestBody MinOrderPriceUpdateRequestDto request){
        return null;
    }

    // 가게 이미지 변경
    @PatchMapping("/stores/{storeId}/image")
    public StoreImageUpdateResponseDto updateStoreImage(
            @PathVariable UUID storeId,
            @RequestParam MultipartFile image){
        return null;
    }

    // 공지사항

    // 배달비 수정
    @PatchMapping
    public StoreDetailResponseDto updateDeliveryFee(@PathVariable UUID storeId, DeliveryFeeUpdateRequestDto request){
        return null;
    }

}
