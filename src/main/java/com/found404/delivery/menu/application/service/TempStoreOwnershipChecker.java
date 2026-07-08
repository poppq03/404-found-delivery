package com.found404.delivery.menu.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Store 도메인 연동 전까지 사용하는 임시 가게 소유자 확인 구현체
 * 추후 Store 도메인의 실제 구현체로 대체
 */
@Slf4j
@Component
public class TempStoreOwnershipChecker implements StoreOwnershipChecker {

    @Override
    public void checkOwner(Long userId, UUID storeId) {
        log.warn("[TEMP] 가게 소유자 확인 생략 (Store 도메인 연동 전): userId={}, storeId={}",
                userId, storeId);
    }
}
