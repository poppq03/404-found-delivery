package com.found404.delivery.domain.menu.service;

import com.found404.delivery.domain.menu.service.MenuInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 다른 도메인에서 메뉴 정보를 조회하기 위한 인터페이스
 */
public interface MenuQueryService {
    MenuInfo getMenuInfo(UUID menuId);

    Map<UUID, MenuInfo> getMenuInfos(List<UUID> menuIds);
}
