package com.found404.delivery.domain.store.controller;

import com.found404.delivery.domain.store.dto.response.StoreSimpleResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreStatusResponseDto;
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
    public StoreStatusResponseDto storeApproval (@PathVariable UUID storeId) {
        return null;
    }

    // 가게 승인 대기 목록?
    @GetMapping ("/stores/approval")
    public List<StoreSimpleResponseDto> storeApprovalList () {
        return null;
    }

    // 가게 상태변경
    @PatchMapping("/stores/{storeId}/suspend")
    public StoreStatusResponseDto updateStoreSuspend(@PathVariable UUID storeId){
        return null;
    }

    // 가게 삭제
    @DeleteMapping("/stores/{storeId}")
    public StoreStatusResponseDto deleteStore(@PathVariable UUID storeId){
        return null;
    }


}
