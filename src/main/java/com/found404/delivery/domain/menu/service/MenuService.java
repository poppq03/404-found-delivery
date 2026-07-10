package com.found404.delivery.domain.menu.service;

import com.found404.delivery.domain.menu.dto.MenuCreateRequestDto;
import com.found404.delivery.domain.menu.dto.MenuCreateResponseDto;
import com.found404.delivery.domain.menu.entity.Menu;
import com.found404.delivery.domain.menu.repository.MenuRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreOwnershipChecker storeOwnershipChecker; // TODO: Store 연동 전 현재는 [TEMP] 주입

    @Transactional
    public MenuCreateResponseDto createMenu(UUID storeId, Long userId, String role, MenuCreateRequestDto createRequest) {

        // 권한 확인
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
                .isAiGenerated(Boolean.TRUE.equals(createRequest.getIsAiGenerated()))
                .build();

        return MenuCreateResponseDto.from(menuRepository.save(menu));
    }
}
