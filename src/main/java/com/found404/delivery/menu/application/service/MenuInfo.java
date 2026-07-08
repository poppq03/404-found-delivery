package com.found404.delivery.menu.application.service;

import java.util.UUID;

/**
 * 다른 도메인에서 메뉴 정보를 조회할 때 사용하는 읽기 전용 DTO
 */
public record MenuInfo(
        UUID menuId,
        UUID storeId,
        String name,
        int price,
        String imageUrl,
        boolean isHidden,
        boolean isSoldOut
) {}
