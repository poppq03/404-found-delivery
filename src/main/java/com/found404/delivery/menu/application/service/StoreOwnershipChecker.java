package com.found404.delivery.menu.application.service;

import java.util.UUID;

public interface StoreOwnershipChecker {
    void checkOwner(Long userId, UUID storeId);
}
