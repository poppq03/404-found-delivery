package com.found404.delivery.domain.store.controller;


import com.found404.delivery.domain.store.dto.response.StoreDetailResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreSimpleResponseDto;
import com.found404.delivery.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreController {

    private final StoreService storeService;


    // 가게 목록 조회
    @GetMapping("/stores")
    public Slice<StoreSimpleResponseDto> getAllStores(
            @PageableDefault(size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        return storeService.getStores(pageable);
    }


    // 카테고리 별 가게 목록 조회 ( 디테일 카테고리 X 큰 카테고리로 검색 )
    @GetMapping("/stores/categories/{category}")
    public Slice<StoreSimpleResponseDto> getCategoryStores(
            @PathVariable UUID category,
            @PageableDefault(size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return storeService.getStoresByCategory(category, pageable);
    }

    // 키워드 가게 검색
    @GetMapping("/stores/search")
    public Slice<StoreSimpleResponseDto> getKeywordStores(
            @RequestParam String keyword,
            @PageableDefault(size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return storeService.searchStoresByKeyword(keyword,pageable);
    }

     //가게 상세 조회
    @GetMapping("/stores/{storeId}")
    public StoreDetailResponseDto getStoreDetail(@PathVariable UUID storeId) {
        return storeService.getStoreDetail(storeId);
    }


}
