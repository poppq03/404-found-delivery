package com.found404.delivery.domain.store.service;

import com.found404.delivery.domain.store.dto.response.StoreDetailResponseDto;
import com.found404.delivery.domain.store.dto.response.StoreSimpleResponseDto;
import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.entity.StoreStatus;
import com.found404.delivery.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreService {

    private final StoreRepository storeRepository;


    // 가게목록 조회
    public Slice<StoreSimpleResponseDto> getStores(Pageable pageable) {
        Slice<Store> stores = storeRepository.findStoreList(pageable);
        return stores.map(StoreSimpleResponseDto::from);
    }

    // 카테고리 별 가게 목록 조회
    @Transactional(readOnly = true)
    public Slice<StoreSimpleResponseDto> getStoresByCategory(UUID categoryId, Pageable pageable) {
        Slice<Store> stores = storeRepository
                .findStoreListByCategory(
                        categoryId,
                        StoreStatus.SUSPENDED,
                        pageable
                );
        return stores.map(StoreSimpleResponseDto::from);
    }


    // 키워드 목록 조회
    public Slice<StoreSimpleResponseDto> searchStoresByKeyword(String keyword, Pageable pageable) {
        Slice<Store> stores = storeRepository.searchStores(
                keyword,
                StoreStatus.SUSPENDED,
                pageable
        );
        return stores.map(StoreSimpleResponseDto::from);
    }


    // 가게 세부사항 조회
    @Transactional(readOnly = true)
    public StoreDetailResponseDto getStoreDetail(UUID storeId) {

        Store store = storeRepository.findByStoreIdAndIsActiveTrueAndStatusNot(
                storeId,
                StoreStatus.SUSPENDED
        ).orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다."));

        return StoreDetailResponseDto.from(store);
    }
}
