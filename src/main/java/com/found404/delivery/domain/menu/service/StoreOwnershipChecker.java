package com.found404.delivery.domain.menu.service;

import java.util.UUID;

public interface StoreOwnershipChecker {

    // 가게 존재 + 소유권 확인 (없으면 STORE_NOT_FOUND, 주인 아니면 NOT_STORE_OWNER)
    void checkOwner(Long userId, UUID storeId);

    // 가게 존재만 확인 (없으면 STORE_NOT_FOUND) - 소유권이 필요 없는 조회용
    void checkStoreExists(UUID storeId);

    // 소유권을 boolean으로 확인 (예외 대신 true/false) - 숨김 조회 판단용
    boolean isOwner(Long userId, UUID storeId);
}