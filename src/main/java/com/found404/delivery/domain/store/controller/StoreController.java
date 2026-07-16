package com.found404.delivery.domain.store.controller;

import com.found404.delivery.domain.store.dto.response.StoreDetailResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreSimpleResponseDto;
import com.found404.delivery.domain.store.service.StoreService;
import com.found404.delivery.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Store - User", description = "유저용 조회 API")
public class StoreController {

    private final StoreService storeService;

    // 가게 목록 조회
    @Operation(
            summary = "가게 목록 조회",
            description = "GET /api/v1/stores"
    )
    @GetMapping("/stores")
    public ApiResponse<Slice<StoreSimpleResponseDto>> getAllStores(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Slice<StoreSimpleResponseDto> response =
                storeService.getStores(pageable);

        return ApiResponse.success(response);
    }

    // 카테고리별 가게 목록 조회
    @Operation(
            summary = "카테고리별 가게 목록 조회",
            description = "GET /api/v1/stores/categories/{categoryId}"
    )
    @GetMapping("/stores/categories/{categoryId}")
    public ApiResponse<Slice<StoreSimpleResponseDto>> getCategoryStores(
            @PathVariable UUID categoryId,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Slice<StoreSimpleResponseDto> response =
                storeService.getStoresByCategory(categoryId, pageable);

        return ApiResponse.success(response);
    }

    // 키워드 가게 검색
    @Operation(
            summary = "가게 키워드 검색",
            description = "GET /api/v1/stores/search?keyword={keyword}"
    )
    @GetMapping("/stores/search")
    public ApiResponse<Slice<StoreSimpleResponseDto>> getKeywordStores(
            @RequestParam String keyword,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Slice<StoreSimpleResponseDto> response =
                storeService.searchStoresByKeyword(keyword, pageable);

        return ApiResponse.success(response);
    }

    // 가게 상세 조회
    @Operation(
            summary = "가게 상세 조회",
            description = "GET /api/v1/stores/{storeId}"
    )
    @GetMapping("/stores/{storeId}")
    public ApiResponse<StoreDetailResponseDto> getStoreDetail(
            @PathVariable UUID storeId
    ) {
        StoreDetailResponseDto response =
                storeService.getStoreDetail(storeId);

        return ApiResponse.success(response);
    }
}