package com.found404.delivery.domain.menu.service;

import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.repository.StoreRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StoreOwnershipCheckerImpl implements StoreOwnershipChecker {

    private final StoreRepository storeRepository;

    // storeId로 가게를 찾고(없으면 STORE_NOT_FOUND),
    // 그 가게의 주인(ownerId는 User 객체)의 id가 요청자와 다르면 NOT_STORE_OWNER
    // ownerId가 LAZY 프록시라 getId()만 부르면 추가 쿼리 없이 FK 값을 꺼냄
    @Override
    public void checkOwner(Long userId, UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getOwner().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_STORE_OWNER);
        }
    }

    // 소유권 없이 존재만 확인 (목록 조회 등)
    @Override
    public void checkStoreExists(UUID storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new CustomException(ErrorCode.STORE_NOT_FOUND);
        }
    }

    // 소유 여부를 boolean으로만 반환 (없는 가게면 false)
    // 숨김 메뉴 조회 판단용
    @Override
    public boolean isOwner(Long userId, UUID storeId) {
        return storeRepository.findById(storeId)
                .map(store -> store.getOwner().getId().equals(userId))
                .orElse(false);
    }
}