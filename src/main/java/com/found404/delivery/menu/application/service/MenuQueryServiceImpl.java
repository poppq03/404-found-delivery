package com.found404.delivery.menu.application.service;

import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import com.found404.delivery.menu.domain.entity.Menu;
import com.found404.delivery.menu.domain.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuQueryServiceImpl implements MenuQueryService {

    private final MenuRepository menuRepository;

    @Override
    public MenuInfo getMenuInfo(UUID menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
        return toInfo(menu);
    }

    @Override
    public Map<UUID, MenuInfo> getMenuInfos(List<UUID> menuIds) {
        // 요청한 메뉴 중 존재하는 메뉴만 menuId를 키로 매핑하여 반환
        // 존재하지 않거나 삭제된 메뉴는 결과에 포함하지 않음
        return menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, this::toInfo));
    }

    private MenuInfo toInfo(Menu menu) {
        return new MenuInfo(menu.getId(), menu.getStoreId(), menu.getName(),
                menu.getPrice(), menu.getImageUrl(), menu.isHidden(), menu.isSoldOut());
    }
}