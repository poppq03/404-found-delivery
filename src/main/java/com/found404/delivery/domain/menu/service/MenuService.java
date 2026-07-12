package com.found404.delivery.domain.menu.service;

import com.found404.delivery.domain.menu.dto.*;
import com.found404.delivery.domain.menu.entity.Menu;
import com.found404.delivery.domain.menu.repository.MenuRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import com.found404.delivery.global.storage.ImageStorage;
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
    private final StoreOwnershipChecker storeOwnershipChecker; // TODO: Store 연동 전 현재는 [TEMP] 주입
    private final ImageStorage imageStorage;

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    // 정렬 허용 필드 (그 외 값은 기본값)
    private static final Set<String> ALLOWED_SORT = Set.of("createdAt", "displayOrder");

    @Transactional
    public MenuCreateResponseDto createMenu(UUID storeId, Long userId, String role,
                                            MenuCreateRequestDto request, MultipartFile image) {

        validateOwnerAccess(role, userId, storeId);

        // save 전에 검증 (확장자만 받아둠)
        String ext = null;
        if (image != null && !image.isEmpty()) {
            ext = validateImage(image);
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

        // 권한 확인 TODO: [TEMP]
        if (menu.isHidden() && !canViewHidden(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return MenuDetailResponseDto.from(menu, resolveImageUrl(menu));
    }

    @Transactional(readOnly = true)
    public MenuListResponseDto getMenus(UUID storeId, String keyword, Boolean soldOut, Long userId, String role, Pageable pageable) {

        // path로 넘어오는 storeId 존재 검증 TODO: Store 연동 후 추가

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

        boolean includeHidden = canViewHidden(role);

        Page<Menu> menuPage = menuRepository.search(storeId, keywordPattern, soldOut, includeHidden, pageable);
        return MenuListResponseDto.from(menuPage, this::resolveImageUrl);
    }

    @Transactional
    public MenuUpdateResponseDto updateMenu(UUID menuId, Long userId, String role,
                                            MenuUpdateRequestDto request, MultipartFile image) {

        Menu menu = getMenuOrThrow(menuId);

        validateOwnerAccess(role, userId, menu.getStoreId());

        menu.update(request.getName(), request.getPrice(), request.getDescription(), request.getDisplayOrder(), request.getAiGenerated());

        // 이미지 교체 또는 제거 시 기존 S3 파일 삭제
        if (image != null && !image.isEmpty()) {
            String ext = validateImage(image);
            String newKey = menuImageKey(menu.getId(), ext);
            String oldKey = menu.getImageUrl();

            imageStorage.upload(newKey, image);
            // 확장자 달라진 경우 기존 파일 제거
            if (oldKey != null && !oldKey.equals(newKey)) {
                imageStorage.delete(oldKey);
            }
            menu.updateImage(newKey);
        } else if (Boolean.TRUE.equals(request.getRemoveImage())) {
            String oldKey = menu.getImageUrl();
            if (oldKey != null) {
                imageStorage.delete(oldKey);
            }
            menu.updateImage(null);
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

        menu.markDeleted(userId);

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

    // image file 크기, 확장자 검증
    private String validateImage(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_SIZE) throw new CustomException(ErrorCode.FILE_TOO_LARGE);
        String name = file.getOriginalFilename();
        String ext = (name != null && name.contains("."))
                ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED_EXT.contains(ext)) throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        return ext;
    }

    // OWNER + 본인 소유 가게 검증 TEMP
    // TODO: UserRole enum + Store 연동 시 추가 구현
    private void validateOwnerAccess(String role, Long userId, UUID storeId) {
        if (!"OWNER".equals(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        storeOwnershipChecker.checkOwner(userId, storeId);
    }

    // 숨김 메뉴 권한 TEMP
    // TODO: UserRole enum 확정되면 교체 + OWNER는 본인 소유 가게일 때 true(userId로 소유 확인, Store 연동 후)
    private boolean canViewHidden(String role) {
        return "OWNER".equals(role) || "MANAGER".equals(role) || "MASTER".equals(role);
    }
}