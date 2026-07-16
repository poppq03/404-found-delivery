package com.found404.delivery.domain.menu.service;

import com.found404.delivery.domain.menu.dto.*;
import com.found404.delivery.domain.menu.entity.Menu;
import com.found404.delivery.domain.menu.repository.MenuRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import com.found404.delivery.global.storage.ImageStorage;
import com.found404.delivery.global.transaction.AfterCommitExecutor;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private StoreOwnershipChecker storeOwnershipChecker;
    @Mock
    private ImageStorage imageStorage;
    @Mock
    private AfterCommitExecutor afterCommitExecutor;

    @InjectMocks
    private MenuService menuService;

    private final UUID storeId = UUID.randomUUID();
    private final UUID menuId = UUID.randomUUID();
    private final Long userId = 1L;

    private Menu baseMenu() {
        Menu menu = Menu.builder()
                .storeId(storeId)
                .name("김밥")
                .price(3000)
                .description("맛있는 김밥")
                .imageUrl(null)
                .displayOrder(0)
                .isAiGenerated(false)
                .build();
        ReflectionTestUtils.setField(menu, "id", menuId);
        return menu;
    }

    private void assertErrorCode(ThrowableAssert.ThrowingCallable callable, ErrorCode expected) {
        assertThatThrownBy(callable)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("createMenu")
    class CreateMenu {

        private MenuCreateRequestDto request() {
            MenuCreateRequestDto dto = new MenuCreateRequestDto();
            dto.setName("김밥");
            dto.setPrice(3000);
            dto.setDescription("맛있는 김밥");
            dto.setDisplayOrder(0);
            dto.setAiGenerated(false);
            return dto;
        }

        @Test
        @DisplayName("이미지 없이 등록 성공 - save 호출, upload 미호출, imageUrl null")
        void success_withoutImage() {
            // JPA save 가 id를 채우는 것을 흉내
            when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> {
                Menu m = inv.getArgument(0);
                ReflectionTestUtils.setField(m, "id", menuId);
                return m;
            });

            MenuCreateResponseDto res =
                    menuService.createMenu(storeId, userId, "OWNER", request(), null);

            assertThat(res.getName()).isEqualTo("김밥");
            assertThat(res.getPrice()).isEqualTo(3000);
            assertThat(res.getImageUrl()).isNull();
            verify(menuRepository).save(any(Menu.class));
            verify(imageStorage, never()).upload(anyString(), any());
            verify(storeOwnershipChecker).checkOwner(userId, storeId);
        }

        @Test
        @DisplayName("이미지와 함께 등록 성공 - menus/{id}.jpg 로 upload, imageUrl은 URL로 변환")
        void success_withImage() {
            MultipartFile image = mock(MultipartFile.class);
            when(image.isEmpty()).thenReturn(false);
            when(imageStorage.validateImage(image)).thenReturn("jpg");
            when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> {
                Menu m = inv.getArgument(0);
                ReflectionTestUtils.setField(m, "id", menuId);
                return m;
            });
            String expectedKey = "menus/" + menuId + ".jpg";
            when(imageStorage.toUrlOrNull(expectedKey))
                    .thenReturn("https://cdn.example.com/" + expectedKey);

            MenuCreateResponseDto res =
                    menuService.createMenu(storeId, userId, "OWNER", request(), image);

            verify(imageStorage).upload(expectedKey, image);
            assertThat(res.getImageUrl()).isEqualTo("https://cdn.example.com/" + expectedKey);
        }

        @Test
        @DisplayName("빈 이미지(isEmpty=true)는 이미지 없는 것으로 처리 - upload 미호출")
        void emptyImage_treatedAsNone() {
            MultipartFile image = mock(MultipartFile.class);
            when(image.isEmpty()).thenReturn(true);
            when(menuRepository.save(any(Menu.class))).thenAnswer(inv -> inv.getArgument(0));

            menuService.createMenu(storeId, userId, "OWNER", request(), image);

            verify(imageStorage, never()).validateImage(any());
            verify(imageStorage, never()).upload(anyString(), any());
        }

        @Test
        @DisplayName("OWNER가 아니면 FORBIDDEN, save 미호출")
        void forbidden_whenNotOwnerRole() {
            assertErrorCode(
                    () -> menuService.createMenu(storeId, userId, "CUSTOMER", request(), null),
                    ErrorCode.FORBIDDEN);
            verify(menuRepository, never()).save(any());
        }

        @Test
        @DisplayName("소유권 검증 실패 시 예외 전파, save 미호출")
        void fail_whenNotStoreOwner() {
            doThrow(new CustomException(ErrorCode.NOT_STORE_OWNER))
                    .when(storeOwnershipChecker).checkOwner(userId, storeId);

            assertErrorCode(
                    () -> menuService.createMenu(storeId, userId, "OWNER", request(), null),
                    ErrorCode.NOT_STORE_OWNER);
            verify(menuRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getMenu")
    class GetMenu {

        @Test
        @DisplayName("공개 메뉴 조회 성공")
        void success_visibleMenu() {
            Menu menu = baseMenu();
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

            MenuDetailResponseDto res = menuService.getMenu(menuId, userId, "CUSTOMER");

            assertThat(res.getMenuId()).isEqualTo(menuId);
            assertThat(res.isHidden()).isFalse();
        }

        @Test
        @DisplayName("존재하지 않으면 MENU_NOT_FOUND")
        void notFound() {
            when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

            assertErrorCode(
                    () -> menuService.getMenu(menuId, userId, "CUSTOMER"),
                    ErrorCode.MENU_NOT_FOUND);
        }

        @Test
        @DisplayName("숨김 메뉴 + 볼 권한 없음(CUSTOMER) → FORBIDDEN")
        void hidden_forbiddenForCustomer() {
            Menu menu = baseMenu();
            menu.changeStatus(true, null); // hidden = true
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

            assertErrorCode(
                    () -> menuService.getMenu(menuId, userId, "CUSTOMER"),
                    ErrorCode.FORBIDDEN);
        }

        @Test
        @DisplayName("숨김 메뉴 + 본인 소유 OWNER → 조회 성공")
        void hidden_allowedForOwnerOfStore() {
            Menu menu = baseMenu();
            menu.changeStatus(true, null);
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
            when(storeOwnershipChecker.isOwner(userId, storeId)).thenReturn(true);

            MenuDetailResponseDto res = menuService.getMenu(menuId, userId, "OWNER");

            assertThat(res.isHidden()).isTrue();
        }

        @Test
        @DisplayName("숨김 메뉴 + MANAGER → 조회 성공(소유권 무관)")
        void hidden_allowedForManager() {
            Menu menu = baseMenu();
            menu.changeStatus(true, null);
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

            MenuDetailResponseDto res = menuService.getMenu(menuId, userId, "MANAGER");

            assertThat(res.isHidden()).isTrue();
            verify(storeOwnershipChecker, never()).isOwner(anyLong(), any());
        }
    }

    // ===== getMenus =====
    @Nested
    @DisplayName("getMenus")
    class GetMenus {

        private Page<Menu> pageOf(Menu... menus) {
            return new PageImpl<>(List.of(menus), PageRequest.of(0, 10), menus.length);
        }

        @Test
        @DisplayName("정상 조회 - content 매핑")
        void success() {
            when(menuRepository.search(eq(storeId), anyString(), any(), anyBoolean(), any()))
                    .thenReturn(pageOf(baseMenu()));

            MenuListResponseDto res = menuService.getMenus(
                    storeId, null, null, userId, "CUSTOMER", PageRequest.of(0, 10));

            assertThat(res.getContent()).hasSize(1);
            assertThat(res.getContent().get(0).getMenuId()).isEqualTo(menuId);
            verify(storeOwnershipChecker).checkStoreExists(storeId);
        }

        @Test
        @DisplayName("허용되지 않은 page size는 10으로 강제")
        void invalidSize_forcedTo10() {
            when(menuRepository.search(any(), anyString(), any(), anyBoolean(), any()))
                    .thenReturn(pageOf());
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

            menuService.getMenus(storeId, null, null, userId, "CUSTOMER", PageRequest.of(0, 20));

            verify(menuRepository).search(any(), anyString(), any(), anyBoolean(), captor.capture());
            assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("정렬 미허용 필드는 무시되고 createdAt desc 기본 적용")
        void invalidSort_defaultsToCreatedAtDesc() {
            when(menuRepository.search(any(), anyString(), any(), anyBoolean(), any()))
                    .thenReturn(pageOf());
            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            Pageable withBadSort = PageRequest.of(0, 10, Sort.by("evilField").ascending());

            menuService.getMenus(storeId, null, null, userId, "CUSTOMER", withBadSort);

            verify(menuRepository).search(any(), anyString(), any(), anyBoolean(), captor.capture());
            Sort.Order createdAt = captor.getValue().getSort().getOrderFor("createdAt");
            assertThat(createdAt).isNotNull();
            assertThat(createdAt.getDirection()).isEqualTo(Sort.Direction.DESC);
            assertThat(captor.getValue().getSort().getOrderFor("evilField")).isNull();
        }

        @Test
        @DisplayName("본인 소유 OWNER는 includeHidden=true 로 검색")
        void ownerIncludesHidden() {
            when(storeOwnershipChecker.isOwner(userId, storeId)).thenReturn(true);
            when(menuRepository.search(any(), anyString(), any(), anyBoolean(), any()))
                    .thenReturn(pageOf());
            ArgumentCaptor<Boolean> includeHidden = ArgumentCaptor.forClass(Boolean.class);

            menuService.getMenus(storeId, null, null, userId, "OWNER", PageRequest.of(0, 10));

            verify(menuRepository).search(
                    any(), anyString(), any(), includeHidden.capture(), any());
            assertThat(includeHidden.getValue()).isTrue();
        }

        @Test
        @DisplayName("가게 없으면 STORE_NOT_FOUND 전파")
        void storeNotFound() {
            doThrow(new CustomException(ErrorCode.STORE_NOT_FOUND))
                    .when(storeOwnershipChecker).checkStoreExists(storeId);

            assertErrorCode(
                    () -> menuService.getMenus(storeId, null, null, userId, "CUSTOMER", PageRequest.of(0, 10)),
                    ErrorCode.STORE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("updateMenu")
    class UpdateMenu {

        private void runAfterCommitImmediately() {
            doAnswer(inv -> {
                ((Runnable) inv.getArgument(0)).run();
                return null;
            }).when(afterCommitExecutor).execute(any());
        }

        private MenuUpdateRequestDto request(String name, Integer price, Boolean removeImage) {
            MenuUpdateRequestDto dto = new MenuUpdateRequestDto();
            dto.setName(name);
            dto.setPrice(price);
            dto.setRemoveImage(removeImage);
            return dto;
        }

        @Test
        @DisplayName("필드만 수정 성공 - 이미지 관련 호출 없음")
        void success_fieldsOnly() {
            Menu menu = baseMenu();
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

            MenuUpdateResponseDto res =
                    menuService.updateMenu(menuId, userId, "OWNER", request("떡볶이", 5000, null), null);

            assertThat(res.getName()).isEqualTo("떡볶이");
            assertThat(res.getPrice()).isEqualTo(5000);
            verify(imageStorage, never()).upload(anyString(), any());
            verify(afterCommitExecutor, never()).execute(any());
        }

        @Test
        @DisplayName("이미지 교체 + 확장자 변경 → 새 이미지 upload 후 옛 이미지 커밋 후 삭제")
        void replaceImage_differentExt_deletesOld() {
            Menu menu = baseMenu();
            menu.updateImage("menus/" + menuId + ".png"); // 기존 png
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
            MultipartFile image = mock(MultipartFile.class);
            when(image.isEmpty()).thenReturn(false);
            when(imageStorage.validateImage(image)).thenReturn("jpg");
            runAfterCommitImmediately();

            menuService.updateMenu(menuId, userId, "OWNER", request(null, null, null), image);

            String newKey = "menus/" + menuId + ".jpg";
            verify(imageStorage).upload(newKey, image);
            verify(imageStorage).delete("menus/" + menuId + ".png");
        }

        @Test
        @DisplayName("이미지 교체 + 동일 확장자(key 동일) → 옛 이미지 삭제 안 함")
        void replaceImage_sameKey_noDelete() {
            Menu menu = baseMenu();
            menu.updateImage("menus/" + menuId + ".jpg");
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
            MultipartFile image = mock(MultipartFile.class);
            when(image.isEmpty()).thenReturn(false);
            when(imageStorage.validateImage(image)).thenReturn("jpg");

            menuService.updateMenu(menuId, userId, "OWNER", request(null, null, null), image);

            verify(imageStorage).upload("menus/" + menuId + ".jpg", image);
            verify(afterCommitExecutor, never()).execute(any());
            verify(imageStorage, never()).delete(anyString());
        }

        @Test
        @DisplayName("removeImage=true → imageUrl null + 옛 이미지 커밋 후 삭제")
        void removeImage() {
            Menu menu = baseMenu();
            menu.updateImage("menus/" + menuId + ".jpg");
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));
            runAfterCommitImmediately();

            menuService.updateMenu(menuId, userId, "OWNER", request(null, null, true), null);

            assertThat(menu.getImageUrl()).isNull();
            verify(imageStorage).delete("menus/" + menuId + ".jpg");
        }

        @Test
        @DisplayName("존재하지 않으면 MENU_NOT_FOUND")
        void notFound() {
            when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

            assertErrorCode(
                    () -> menuService.updateMenu(menuId, userId, "OWNER", request("x", null, null), null),
                    ErrorCode.MENU_NOT_FOUND);
        }

        @Test
        @DisplayName("OWNER 아니면 FORBIDDEN")
        void forbidden() {
            Menu menu = baseMenu();
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

            assertErrorCode(
                    () -> menuService.updateMenu(menuId, userId, "CUSTOMER", request("x", null, null), null),
                    ErrorCode.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("changeStatus")
    class ChangeStatus {

        private MenuStatusRequestDto request(Boolean hidden, Boolean soldOut) {
            MenuStatusRequestDto dto = new MenuStatusRequestDto();
            ReflectionTestUtils.setField(dto, "hidden", hidden);
            ReflectionTestUtils.setField(dto, "soldOut", soldOut);
            return dto;
        }

        @Test
        @DisplayName("상태 변경 성공")
        void success() {
            Menu menu = baseMenu();
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

            MenuStatusResponseDto res =
                    menuService.changeStatus(menuId, userId, "OWNER", request(true, true));

            assertThat(res.isHidden()).isTrue();
            assertThat(res.isSoldOut()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않으면 MENU_NOT_FOUND")
        void notFound() {
            when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

            assertErrorCode(
                    () -> menuService.changeStatus(menuId, userId, "OWNER", request(true, null)),
                    ErrorCode.MENU_NOT_FOUND);
        }

        @Test
        @DisplayName("OWNER 아니면 FORBIDDEN")
        void forbidden() {
            Menu menu = baseMenu();
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

            assertErrorCode(
                    () -> menuService.changeStatus(menuId, userId, "MANAGER", request(true, null)),
                    ErrorCode.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("deleteMenu")
    class DeleteMenu {

        @Test
        @DisplayName("soft delete 성공 - deletedAt 설정")
        void success() {
            Menu menu = baseMenu();
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

            MenuDeleteResponseDto res = menuService.deleteMenu(menuId, userId, "OWNER");

            assertThat(res.getMenuId()).isEqualTo(menuId);
            assertThat(menu.getDeletedAt()).isNotNull();
            verify(imageStorage, never()).delete(anyString());
        }

        @Test
        @DisplayName("존재하지 않으면 MENU_NOT_FOUND")
        void notFound() {
            when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

            assertErrorCode(
                    () -> menuService.deleteMenu(menuId, userId, "OWNER"),
                    ErrorCode.MENU_NOT_FOUND);
        }

        @Test
        @DisplayName("OWNER 아니면 FORBIDDEN")
        void forbidden() {
            Menu menu = baseMenu();
            when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

            assertErrorCode(
                    () -> menuService.deleteMenu(menuId, userId, "CUSTOMER"),
                    ErrorCode.FORBIDDEN);
        }
    }
}
