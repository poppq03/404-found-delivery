package com.found404.delivery.domain.menu.service;

import java.util.UUID;

public interface StoreOwnershipChecker {
    void checkOwner(Long userId, UUID storeId);
}
