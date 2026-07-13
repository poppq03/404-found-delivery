package com.found404.delivery.domain.store.controller;

import com.found404.delivery.domain.store.dto.response.StoreSimpleResponse;
import com.found404.delivery.domain.store.dto.response.StoreStatusResponse;
import com.found404.delivery.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class StoreMasterController {

    private final StoreService storeService;


    // 가게 승인
    @PatchMapping("/stores/{storeId}/approve")
    public StoreStatusResponse storeApproval (@PathVariable UUID storeId) {
        return null;
    }

    // 가게 승인 대기 목록?
    @GetMapping ("/stores/approval")
    public List<StoreSimpleResponse> storeApprovalList () {
        return null;
    }
    // 가게 정지
    @PatchMapping("/stores/{storeId}/suspend")
    public StoreStatusResponse updateStoreSuspend(@PathVariable UUID storeId){
        return null;
    }

    // 정지 해제
    @PatchMapping("/stores/{storeId}/activate")
    public StoreStatusResponse updateStoreActivate(@PathVariable UUID storeId){
        return null;
    }
    // 가게 삭제
    @DeleteMapping("/stores/{storeId}")
    public StoreStatusResponse deleteStore(@PathVariable UUID storeId){
        return null;
    }


}
