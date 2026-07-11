package com.found404.delivery.domain.menu.service;

import com.found404.delivery.domain.menu.dto.*;
import com.found404.delivery.domain.menu.entity.Menu;
import com.found404.delivery.domain.menu.repository.MenuRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreOwnershipChecker storeOwnershipChecker; // TODO: Store 연동 전 현재는 [TEMP] 주입

    // 숨김 메뉴 권한 헬퍼 TODO: UserRole enum 확정되면 교체 + OWNER는 본인 소유 가게일 때 true(userId로 소유 확인, Store 연동 후)
    private boolean canViewHidden(String role) {
        return "OWNER".equals(role) || "MANAGER".equals(role) || "MASTER".equals(role);
    }

    @Transactional
    public MenuCreateResponseDto createMenu(UUID storeId, Long userId, String role, MenuCreateRequestDto createRequest) {

        // 권한 확인 TODO: UserRole enum 확정되면 교체
        if (!"OWNER".equals(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        // 가게 소유자 검증 TODO: 현재는 [TEMP]로 통과, Store 연동 후 실제로 검증
        storeOwnershipChecker.checkOwner(userId, storeId);

        Menu menu = Menu.builder()
                .storeId(storeId)
                .name(createRequest.getName())
                .price(createRequest.getPrice())
                .description(createRequest.getDescription())
                .imageUrl(null) // TODO: S3 연동 시 구현
                .displayOrder(createRequest.getDisplayOrder())
                .isAiGenerated(Boolean.TRUE.equals(createRequest.getAiGenerated()))
                .build();

        return MenuCreateResponseDto.from(menuRepository.save(menu));
    }

    @Transactional(readOnly = true)
    public MenuDetailResponseDto getMenu(UUID menuId, Long userId, String role) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 권한 확인 TODO: UserRole enum 확정되면 교체
        if (menu.isHidden() && !canViewHidden(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return MenuDetailResponseDto.from(menu);
    }

    @Transactional(readOnly = true)
    public MenuListResponseDto getMenus(UUID storeId, String keyword, Boolean soldOut, Long userId, String role, Pageable pageable) {

        // path로 넘어오는 storeId 존재 검증 TODO: Store 연동 후 추가

        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            pageable = PageRequest.of(pageable.getPageNumber(), 10, pageable.getSort());
        }

        // keyword null 값 처리
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? "%"
                : "%" + keyword + "%";

        // 권한 확인 TODO: UserRole enum 확정되면 교체
        boolean includeHidden = canViewHidden(role);

        Page<Menu> menuPage = menuRepository.search(storeId, keywordPattern, soldOut, includeHidden, pageable);
        return MenuListResponseDto.from(menuPage);
    }

    @Transactional
    public MenuUpdateResponseDto updateMenu(UUID menuId, Long userId, String role, MenuUpdateRequestDto updateRequest) {

        // 권한 확인 TODO: UserRole enum 확정되면 교체
        if (!"OWNER".equals(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));

        // 가게 소유자 검증 TODO: 현재는 [TEMP]로 통과, Store 연동 후 실제로 검증
        storeOwnershipChecker.checkOwner(userId, menu.getStoreId());

        menu.update(updateRequest.getName(), updateRequest.getPrice(), updateRequest.getDescription(), updateRequest.getDisplayOrder(), updateRequest.getAiGenerated());

        return MenuUpdateResponseDto.from(menu);
    }
}
