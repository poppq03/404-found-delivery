package com.found404.delivery.domain.store.controller;


import com.found404.delivery.domain.store.dto.response.StoreDetailResponse;
import com.found404.delivery.domain.store.dto.response.StoreSimpleResponse;
import com.found404.delivery.domain.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class StoreController {

    private final StoreService storeService;


    // 가게 목록 조회
    @GetMapping("/stores")
    public List<StoreSimpleResponse> getAllStores() {

        return null;
    }

    // 카테고리 별 가게 목록 조회 ( 디테일 카테고리 X 큰 카테고리로 검색 )

    @GetMapping("/stores/{category}")
    public List<StoreSimpleResponse> getCategoryStores(@PathVariable String category) {

        return null;
    }

    // 키워드 가게 검색 /stores/keyword?=~~~~
    @GetMapping("/stores/{keyword}")
    public List<StoreSimpleResponse> getKeywordStores(@PathVariable String keyword) {

        return null;
    }


    // 가게 상세 조회
    @GetMapping("/stores/{storeId}")
    public StoreDetailResponse getStoreDetail(@PathVariable UUID storeId) {

        return null;
    }




}
