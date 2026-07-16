package com.found404.delivery.domain.cart.service;

import com.found404.delivery.domain.cart.dto.CartAddRequestDto;
import com.found404.delivery.domain.cart.dto.CartResponseDto;
import com.found404.delivery.domain.cart.entity.Cart;
import com.found404.delivery.domain.cart.repository.CartRepository;
import com.found404.delivery.domain.cartitem.dto.CartItemQuantityRequestDto;
import com.found404.delivery.domain.cartitem.entity.CartItem;
import com.found404.delivery.domain.cartitem.repository.CartItemRepository;
import com.found404.delivery.domain.menu.service.MenuInfo;
import com.found404.delivery.domain.menu.service.MenuQueryService;
import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.repository.StoreRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.assertj.core.api.ThrowableAssert;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private MenuQueryService menuQueryService;
    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private CartService cartService;

    private final Long userId = 1L;
    private final UUID storeA = UUID.randomUUID();
    private final UUID storeB = UUID.randomUUID();
    private final UUID menuA = UUID.randomUUID();
    private final UUID menuB = UUID.randomUUID();
    private final UUID cartItemId = UUID.randomUUID();


    private MenuInfo menuInfo(UUID menuId, UUID storeId, int price, boolean hidden, boolean soldOut) {
        return new MenuInfo(menuId, storeId, "김밥", price, "menus/x.jpg", hidden, soldOut);
    }

    private MenuInfo availableMenu(UUID menuId, UUID storeId, int price) {
        return menuInfo(menuId, storeId, price, false, false);
    }

    // 아이템 1개가 담긴 실제 Cart (storeId 세팅됨)
    private Cart cartWithItem(UUID storeId, UUID menuId, int quantity) {
        Cart cart = Cart.builder().userId(userId).build();
        cart.addItem(menuId, quantity, storeId); // storeId 지정 + CartItem 추가(양방향 세팅)
        return cart;
    }

    private CartAddRequestDto addRequest(UUID menuId, int quantity) {
        CartAddRequestDto dto = new CartAddRequestDto();
        dto.setMenuId(menuId);
        dto.setQuantity(quantity);
        return dto;
    }

    private CartItemQuantityRequestDto qtyRequest(int quantity) {
        CartItemQuantityRequestDto dto = new CartItemQuantityRequestDto();
        dto.setQuantity(quantity);
        return dto;
    }

    private Store store(String name, int minOrderPrice) {
        return Store.builder().name(name).minOrderPrice(minOrderPrice).build();
    }

    private void assertErrorCode(ThrowableAssert.ThrowingCallable callable, ErrorCode expected) {
        assertThatThrownBy(callable)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(expected);
    }

    @Nested
    @DisplayName("getCart")
    class GetCart {

        @Test
        @DisplayName("장바구니 없으면 빈 응답")
        void emptyCart() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

            CartResponseDto res = cartService.getCart(userId, "CUSTOMER");

            assertThat(res.getCartId()).isNull();
            assertThat(res.getItems()).isEmpty();
            assertThat(res.getTotalQuantity()).isZero();
            assertThat(res.getTotalMenuPrice()).isZero();
            verify(menuQueryService, never()).getMenuInfos(any());
        }

        @Test
        @DisplayName("항목이 있으면 메뉴 병합 + 합계 + 가게 정보 반환")
        void withItems() {
            Cart cart = cartWithItem(storeA, menuA, 2);
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(menuQueryService.getMenuInfos(anyList()))
                    .thenReturn(Map.of(menuA, availableMenu(menuA, storeA, 3000)));
            when(storeRepository.findById(storeA)).thenReturn(Optional.of(store("김밥천국", 12000)));

            CartResponseDto res = cartService.getCart(userId, "CUSTOMER");

            assertThat(res.getItems()).hasSize(1);
            assertThat(res.getItems().get(0).getQuantity()).isEqualTo(2);
            assertThat(res.getItems().get(0).getItemTotalPrice()).isEqualTo(6000); // 3000 * 2
            assertThat(res.getTotalQuantity()).isEqualTo(2);
            assertThat(res.getTotalMenuPrice()).isEqualTo(6000);
            assertThat(res.getStoreName()).isEqualTo("김밥천국");
            assertThat(res.getMinOrderPrice()).isEqualTo(12000);
        }

        @Test
        @DisplayName("삭제된 메뉴(조회 Map에 없음)는 응답/합계에서 제외")
        void skipsDeletedMenu() {
            Cart cart = cartWithItem(storeA, menuA, 2);
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(menuQueryService.getMenuInfos(anyList())).thenReturn(Map.of()); // 메뉴가 사라짐
            when(storeRepository.findById(storeA)).thenReturn(Optional.of(store("김밥천국", 12000)));

            CartResponseDto res = cartService.getCart(userId, "CUSTOMER");

            assertThat(res.getItems()).isEmpty();
            assertThat(res.getTotalQuantity()).isZero();
        }

        @Test
        @DisplayName("CUSTOMER가 아니면 FORBIDDEN")
        void forbidden() {
            assertErrorCode(() -> cartService.getCart(userId, "OWNER"), ErrorCode.FORBIDDEN);
            verify(cartRepository, never()).findByUserId(any());
        }
    }

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        @DisplayName("빈 유저 - 카트 생성 후 담기, storeReplaced=false")
        void newCart() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
            when(menuQueryService.getMenuInfo(menuA)).thenReturn(availableMenu(menuA, storeA, 3000));
            when(menuQueryService.getMenuInfos(anyList()))
                    .thenReturn(Map.of(menuA, availableMenu(menuA, storeA, 3000)));
            when(storeRepository.findById(storeA)).thenReturn(Optional.of(store("김밥천국", 12000)));

            CartResponseDto res = cartService.addItem(userId, "CUSTOMER", addRequest(menuA, 2));

            assertThat(res.getStoreReplaced()).isFalse();
            assertThat(res.getStoreId()).isEqualTo(storeA);
            assertThat(res.getItems()).hasSize(1);
            assertThat(res.getTotalQuantity()).isEqualTo(2);
            verify(cartRepository).save(any(Cart.class)); // 신규 카트 생성
        }

        @Test
        @DisplayName("같은 가게 같은 메뉴 재담기 - 수량 합산")
        void sameMenu_mergesQuantity() {
            Cart cart = cartWithItem(storeA, menuA, 2);
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(menuQueryService.getMenuInfo(menuA)).thenReturn(availableMenu(menuA, storeA, 3000));
            when(menuQueryService.getMenuInfos(anyList()))
                    .thenReturn(Map.of(menuA, availableMenu(menuA, storeA, 3000)));
            when(storeRepository.findById(storeA)).thenReturn(Optional.of(store("김밥천국", 12000)));

            CartResponseDto res = cartService.addItem(userId, "CUSTOMER", addRequest(menuA, 3));

            assertThat(res.getItems()).hasSize(1);
            assertThat(res.getItems().get(0).getQuantity()).isEqualTo(5); // 2 + 3
            assertThat(res.getTotalQuantity()).isEqualTo(5);
            assertThat(res.getStoreReplaced()).isFalse();
        }

        @Test
        @DisplayName("다른 가게 메뉴 담기 - 기존 카트 비우고 교체, storeReplaced=true")
        void differentStore_replacesCart() {
            Cart cart = cartWithItem(storeA, menuA, 2);
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(menuQueryService.getMenuInfo(menuB)).thenReturn(availableMenu(menuB, storeB, 5000));
            when(menuQueryService.getMenuInfos(anyList()))
                    .thenReturn(Map.of(menuB, availableMenu(menuB, storeB, 5000)));
            when(storeRepository.findById(storeB)).thenReturn(Optional.of(store("떡볶이집", 8000)));

            CartResponseDto res = cartService.addItem(userId, "CUSTOMER", addRequest(menuB, 1));

            assertThat(res.getStoreReplaced()).isTrue();
            assertThat(res.getStoreId()).isEqualTo(storeB);
            assertThat(res.getItems()).hasSize(1);
            assertThat(res.getItems().get(0).getMenuId()).isEqualTo(menuB);
            assertThat(res.getTotalQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("숨김/품절 메뉴는 MENU_UNAVAILABLE - 카트 미조회")
        void unavailableMenu() {
            when(menuQueryService.getMenuInfo(menuA))
                    .thenReturn(menuInfo(menuA, storeA, 3000, false, true)); // 품절

            assertErrorCode(
                    () -> cartService.addItem(userId, "CUSTOMER", addRequest(menuA, 1)),
                    ErrorCode.MENU_UNAVAILABLE);
            verify(cartRepository, never()).findByUserId(any());
        }

        @Test
        @DisplayName("수량 1 미만이면 INVALID_QUANTITY")
        void invalidQuantity() {
            when(menuQueryService.getMenuInfo(menuA)).thenReturn(availableMenu(menuA, storeA, 3000));

            assertErrorCode(
                    () -> cartService.addItem(userId, "CUSTOMER", addRequest(menuA, 0)),
                    ErrorCode.INVALID_QUANTITY);
            verify(cartRepository, never()).findByUserId(any());
        }

        @Test
        @DisplayName("CUSTOMER가 아니면 FORBIDDEN - 메뉴 조회도 안 함")
        void forbidden() {
            assertErrorCode(
                    () -> cartService.addItem(userId, "OWNER", addRequest(menuA, 1)),
                    ErrorCode.FORBIDDEN);
            verify(menuQueryService, never()).getMenuInfo(any());
        }
    }

    @Nested
    @DisplayName("changeQuantity")
    class ChangeQuantity {

        private CartItem seededItem(Cart cart) {
            CartItem item = cart.getItems().get(0);
            ReflectionTestUtils.setField(item, "id", cartItemId);
            return item;
        }

        @Test
        @DisplayName("수량 변경 성공")
        void success() {
            Cart cart = cartWithItem(storeA, menuA, 2);
            CartItem item = seededItem(cart);
            when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));
            when(menuQueryService.getMenuInfos(anyList()))
                    .thenReturn(Map.of(menuA, availableMenu(menuA, storeA, 3000)));
            when(storeRepository.findById(storeA)).thenReturn(Optional.of(store("김밥천국", 12000)));

            CartResponseDto res = cartService.changeQuantity(userId, "CUSTOMER", cartItemId, qtyRequest(4));

            assertThat(res.getItems().get(0).getQuantity()).isEqualTo(4);
            assertThat(res.getTotalQuantity()).isEqualTo(4);
            assertThat(res.getTotalMenuPrice()).isEqualTo(12000); // 3000 * 4
        }

        @Test
        @DisplayName("수량 1 미만이면 INVALID_QUANTITY - 항목 조회 안 함")
        void invalidQuantity() {
            assertErrorCode(
                    () -> cartService.changeQuantity(userId, "CUSTOMER", cartItemId, qtyRequest(0)),
                    ErrorCode.INVALID_QUANTITY);
            verify(cartItemRepository, never()).findById(any());
        }

        @Test
        @DisplayName("항목이 없으면 CART_ITEM_NOT_FOUND")
        void notFound() {
            when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

            assertErrorCode(
                    () -> cartService.changeQuantity(userId, "CUSTOMER", cartItemId, qtyRequest(2)),
                    ErrorCode.CART_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("남의 장바구니 항목이면 CART_ITEM_NOT_FOUND")
        void notOwner() {
            Cart otherCart = Cart.builder().userId(999L).build(); // 다른 유저
            otherCart.addItem(menuA, 2, storeA);
            CartItem item = seededItem(otherCart);
            when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));

            assertErrorCode(
                    () -> cartService.changeQuantity(userId, "CUSTOMER", cartItemId, qtyRequest(2)),
                    ErrorCode.CART_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("CUSTOMER가 아니면 FORBIDDEN")
        void forbidden() {
            assertErrorCode(
                    () -> cartService.changeQuantity(userId, "OWNER", cartItemId, qtyRequest(2)),
                    ErrorCode.FORBIDDEN);
            verify(cartItemRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("removeItem")
    class RemoveItem {

        @Test
        @DisplayName("항목 삭제 성공 - 마지막 항목이면 빈 카트 + storeId null")
        void success() {
            Cart cart = cartWithItem(storeA, menuA, 2);
            CartItem item = cart.getItems().get(0);
            ReflectionTestUtils.setField(item, "id", cartItemId);
            when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.of(item));
            when(menuQueryService.getMenuInfos(anyList())).thenReturn(Map.of());

            CartResponseDto res = cartService.removeItem(userId, "CUSTOMER", cartItemId);

            assertThat(res.getItems()).isEmpty();
            assertThat(res.getTotalQuantity()).isZero();
            assertThat(res.getStoreId()).isNull(); // 마지막 항목 제거 시 가게 해제
            verify(storeRepository, never()).findById(any()); // storeId null → 가게 조회 스킵함
        }

        @Test
        @DisplayName("항목이 없으면 CART_ITEM_NOT_FOUND")
        void notFound() {
            when(cartItemRepository.findById(cartItemId)).thenReturn(Optional.empty());

            assertErrorCode(
                    () -> cartService.removeItem(userId, "CUSTOMER", cartItemId),
                    ErrorCode.CART_ITEM_NOT_FOUND);
        }

        @Test
        @DisplayName("CUSTOMER가 아니면 FORBIDDEN")
        void forbidden() {
            assertErrorCode(
                    () -> cartService.removeItem(userId, "OWNER", cartItemId),
                    ErrorCode.FORBIDDEN);
            verify(cartItemRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("clearCart")
    class ClearCart {

        @Test
        @DisplayName("카트 비우기 성공 - items 비고 storeId null")
        void success() {
            Cart cart = cartWithItem(storeA, menuA, 2);
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
            when(menuQueryService.getMenuInfos(anyList())).thenReturn(Map.of());

            CartResponseDto res = cartService.clearCart(userId, "CUSTOMER");

            assertThat(res.getItems()).isEmpty();
            assertThat(res.getStoreId()).isNull();
        }

        @Test
        @DisplayName("카트 없으면 빈 응답")
        void noCart() {
            when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

            CartResponseDto res = cartService.clearCart(userId, "CUSTOMER");

            assertThat(res.getCartId()).isNull();
            assertThat(res.getItems()).isEmpty();
            verify(menuQueryService, never()).getMenuInfos(any());
        }

        @Test
        @DisplayName("CUSTOMER가 아니면 FORBIDDEN")
        void forbidden() {
            assertErrorCode(() -> cartService.clearCart(userId, "OWNER"), ErrorCode.FORBIDDEN);
            verify(cartRepository, never()).findByUserId(any());
        }
    }
}
