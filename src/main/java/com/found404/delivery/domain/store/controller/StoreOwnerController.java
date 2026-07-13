package com.found404.delivery.domain.store.controller;

import com.found404.delivery.domain.store.dto.request.*;
import com.found404.delivery.domain.store.dto.response.StoreDetailResponse;
import com.found404.delivery.domain.store.dto.response.StoreImageUpdateResponse;
import com.found404.delivery.domain.store.dto.response.StoreStatusResponse;
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
    public StoreDetailResponse createStore(@RequestBody StoreCreateRequest request){
        return null;
    }

    // 가게 수정
    @PatchMapping("/stores/{storeId}")
    public StoreUpdateRequest updateStore(@PathVariable UUID storeId, @RequestBody StoreUpdateRequest request){

        return null;
    }

    // 가게 삭제
    @DeleteMapping("/stores/{storeId}")
    public StoreStatusResponse deleteStore(){

        return null;
    }

    // 영업상태 변경
    @PatchMapping("/stores/{storeId}/status")
    public StoreStatusResponse updateStoreStatus(@PathVariable UUID storeId, @RequestBody StoreStatusRequest request){
        return null;
    }

    // 영업 Open
    @PatchMapping("/stores/{storeId}/open")
    public StoreStatusResponse openStore(@PathVariable UUID storeId, @RequestBody StoreStatusRequest request){
        return null;
    }
    // 영업 Close
    @PatchMapping("/stores/{storeId}/close")
    public StoreStatusResponse closeStore(@PathVariable UUID storeId, @RequestBody StoreStatusRequest request){
        return null;
    }

    // 최소 주문 금액 수정
    @PatchMapping("/stores/{storeId}/minimumOrderPrice")
    public StoreDetailResponse updateMinOrderPrice(@PathVariable UUID storeId, @RequestBody MinOrderPriceUpdateRequest request){
        return null;
    }

    // 가게 이미지 변경
    @PatchMapping("/stores/{storeId}/image")
    public StoreImageUpdateResponse updateStoreImage(
            @PathVariable UUID storeId,
            @RequestParam MultipartFile image){
        return null;
    }

    // 공지사항

    // 배달비 수정
    @PatchMapping
    public StoreDetailResponse updateDeliveryFee(@PathVariable UUID storeId, DeliveryFeeUpdateRequest request){
        return null;
    }

}
