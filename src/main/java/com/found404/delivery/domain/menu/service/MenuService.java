package com.found404.delivery.domain.menu.service;

import com.found404.delivery.domain.menu.dto.*;
import com.found404.delivery.domain.menu.entity.Menu;
import com.found404.delivery.domain.menu.repository.MenuRepository;
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import com.found404.delivery.global.storage.ImageStorage;
import com.found404.delivery.global.transaction.AfterCommitExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreOwnershipChecker storeOwnershipChecker; // 실제 구현체(StoreOwnershipCheckerImpl) 주입
    private final ImageStorage imageStorage;
    private final AfterCommitExecutor afterCommitExecutor;

    // 정렬 허용 필드 (그 외 값은 기본값)
    private static final Set<String> ALLOWED_SORT = Set.of("createdAt", "displayOrder");

    @Transactional
    public MenuCreateResponseDto createMenu(UUID storeId, Long userId, String role,
                                            MenuCreateRequestDto request, MultipartFile image) {

        validateOwnerAccess(role, userId, storeId);

        // save 전에 검증 (확장자만 받아둠)
        String ext = null;
        if (image != null && !image.isEmpty()) {
            ext = imageStorage.validateImage(image);
        }

        Menu menu = Menu.builder()
                .storeId(storeId)
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .imageUrl(null) // save 후 dirty checking으로 key 채움
                .displayOrder(request.getDisplayOrder())
                .isAiGenerated(Boolean.TRUE.equals(request.getAiGenerated()))
                .build();

        menuRepository.save(menu);

        // image key 조립
        if (ext != null) {
            String key = menuImageKey(menu.getId(), ext);
            imageStorage.upload(key, image);
            menu.updateImage(key);
        }

        return MenuCreateResponseDto.from(menu, resolveImageUrl(menu));
    }

    @Transactional(readOnly = true)
    public MenuDetailResponseDto getMenu(UUID menuId, Long userId, String role) {

        Menu menu = getMenuOrThrow(menuId);

        // 숨김 메뉴는 볼 수 있는 권한(OWNER/MANAGER/MASTER)일 때만 조회 허용
        if (menu.isHidden() && !canViewHidden(role, userId, menu.getStoreId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return MenuDetailResponseDto.from(menu, resolveImageUrl(menu));
    }

    @Transactional(readOnly = true)
    public MenuListResponseDto getMenus(UUID storeId, String keyword, Boolean soldOut, Long userId, String role, Pageable pageable) {

        // path로 넘어오는 storeId 존재 검증 (없으면 STORE_NOT_FOUND)
        storeOwnershipChecker.checkStoreExists(storeId);

        // 정렬: 허용 필드(createdAt, displayOrder) -> 그 외는 버림
        List<Sort.Order> orders = pageable.getSort().stream()
                .filter(order -> ALLOWED_SORT.contains(order.getProperty()))
                .collect(Collectors.toList());

        // 정렬 기본값 설정
        if (orders.isEmpty()) {
            orders.add(Sort.Order.desc("createdAt"));
        }

        // displayOrder 정렬 시 값이 같으면 생성일 순으로 재정렬
        boolean hasCreatedAt = orders.stream().anyMatch(order -> order.getProperty().equals("createdAt"));
        if (!hasCreatedAt) {
            orders.add(Sort.Order.desc("createdAt"));
        }

        int size = pageable.getPageSize();

        // size 기본값 설정
        if (size != 10 && size != 30 && size != 50) {
            size = 10;
        }

        pageable = PageRequest.of(pageable.getPageNumber(), size, Sort.by(orders));

        // keyword null 값 처리
        String keywordPattern = (keyword == null || keyword.isBlank())
                ? "%"
                : "%" + keyword + "%";

        boolean includeHidden = canViewHidden(role, userId, storeId);

        Page<Menu> menuPage = menuRepository.search(storeId, keywordPattern, soldOut, includeHidden, pageable);
        return MenuListResponseDto.from(menuPage, this::resolveImageUrl);
    }

    @Transactional
    public MenuUpdateResponseDto updateMenu(UUID menuId, Long userId, String role,
                                            MenuUpdateRequestDto request, MultipartFile image) {

        Menu menu = getMenuOrThrow(menuId);

        validateOwnerAccess(role, userId, menu.getStoreId());

        menu.update(request.getName(), request.getPrice(), request.getDescription(),
                request.getDisplayOrder(), request.getAiGenerated());

        if (image != null && !image.isEmpty()) {
            String ext = imageStorage.validateImage(image);
            String newKey = menuImageKey(menu.getId(), ext);
            String oldKey = menu.getImageUrl();

            imageStorage.upload(newKey, image);
            menu.updateImage(newKey);

            // 트랜잭션 커밋 후 기존 이미지 삭제 (확장자가 바뀐 경우)
            if (oldKey != null && !oldKey.equals(newKey)) {
                afterCommitExecutor.execute(() -> imageStorage.delete(oldKey));
            }
        } else if (Boolean.TRUE.equals(request.getRemoveImage())) {
            String oldKey = menu.getImageUrl();
            menu.updateImage(null);

            // 트랜잭션 커밋 후 기존 이미지 삭제
            if (oldKey != null) {
                afterCommitExecutor.execute(() -> imageStorage.delete(oldKey));
            }
        }

        return MenuUpdateResponseDto.from(menu, resolveImageUrl(menu));
    }

    @Transactional
    public MenuStatusResponseDto changeStatus(UUID menuId, Long userId, String role, MenuStatusRequestDto request) {

        Menu menu = getMenuOrThrow(menuId);

        validateOwnerAccess(role, userId, menu.getStoreId());

        menu.changeStatus(request.getHidden(), request.getSoldOut());

        return MenuStatusResponseDto.from(menu);
    }

    @Transactional
    public MenuDeleteResponseDto deleteMenu(UUID menuId, Long userId, String role) {

        Menu menu = getMenuOrThrow(menuId);

        validateOwnerAccess(role, userId, menu.getStoreId());

        menu.markDeleted(userId); // soft delete, S3 파일도 삭제되지 않음

        return MenuDeleteResponseDto.from(menu);
    }

    // ===== private 헬퍼 =====

    // 메뉴 조회 (없으면 404)
    private Menu getMenuOrThrow(UUID menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new CustomException(ErrorCode.MENU_NOT_FOUND));
    }

    // 메뉴 이미지 S3 key
    private String menuImageKey(UUID menuId, String ext) {
        return "menus/" + menuId + "." + ext;
    }

    // DB에 저장된 S3 key → URL 변환, 이미지 없을 시 null
    private String resolveImageUrl(Menu menu) {
        return imageStorage.toUrlOrNull(menu.getImageUrl());
    }

    // OWNER + 본인 소유 가게 검증
    private void validateOwnerAccess(String role, Long userId, UUID storeId) {
        if (Role.valueOf(role) != Role.OWNER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        storeOwnershipChecker.checkOwner(userId, storeId);
    }

    // 숨김 메뉴 조회 권한: 관리자(MANAGER/MASTER)는 전부, OWNER는 본인 소유 가게만
    private boolean canViewHidden(String role, Long userId, UUID storeId) {
        Role r = Role.valueOf(role);
        if (r == Role.MANAGER || r == Role.MASTER) {
            return true;
        }
        if (r == Role.OWNER) {
            return storeOwnershipChecker.isOwner(userId, storeId);
        }
        return false;
    }
}