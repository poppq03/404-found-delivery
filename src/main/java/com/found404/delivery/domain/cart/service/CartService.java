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
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuQueryService menuQueryService;
    private final StoreRepository storeRepository;


    @Transactional(readOnly = true)
    public CartResponseDto getCart(Long userId, String role) {
        validateCustomerAccess(role);

        return cartRepository.findByUserId(userId)
                .map(cart -> buildCartResponse(cart, null))
                .orElseGet(this::emptyCartResponse);
    }

    @Transactional
    public CartResponseDto addItem(Long userId, String role, CartAddRequestDto request) {
        validateCustomerAccess(role);

        MenuInfo menu = menuQueryService.getMenuInfo(request.getMenuId());
        if (menu.isHidden() || menu.isSoldOut()) {
            throw new CustomException(ErrorCode.MENU_UNAVAILABLE);
        }

        if (request.getQuantity() < 1) {
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }

        Cart cart = getOrCreateCart(userId);

        boolean storeReplaced = false;
        if (cart.getStoreId() != null && !cart.getStoreId().equals(menu.storeId())) {
            cart.clearCart();
            storeReplaced = true;
        }

        cart.addItem(menu.menuId(), request.getQuantity(), menu.storeId());
        return buildCartResponse(cart, storeReplaced);
    }

    @Transactional
    public CartResponseDto changeQuantity(Long userId, String role, UUID cartItemId,
                                          CartItemQuantityRequestDto request) {
        validateCustomerAccess(role);

        if (request.getQuantity() < 1) {
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }

        CartItem cartItem = getCartItemOrThrow(cartItemId, userId);
        cartItem.changeQuantity(request.getQuantity());

        return buildCartResponse(cartItem.getCart(), null);
    }

    @Transactional
    public CartResponseDto removeItem(Long userId, String role, UUID cartItemId) {
        validateCustomerAccess(role);

        CartItem cartItem = getCartItemOrThrow(cartItemId, userId);
        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem);

        return buildCartResponse(cart, null);
    }

    @Transactional
    public CartResponseDto clearCart(Long userId, String role) {
        validateCustomerAccess(role);

        return cartRepository.findByUserId(userId)
                .map(cart -> {
                    cart.clearCart();
                    return buildCartResponse(cart, null);
                })
                .orElseGet(this::emptyCartResponse);
    }


    // ===== private 헬퍼 =====


    // 유저 장바구니 조회, 없으면 생성 (담기에서만 사용)
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().userId(userId).build()));
    }

    // cartItemId로 항목 조회 + 본인 장바구니 소속 검증 (남의 것/없는 것 모두 404 처리)
    private CartItem getCartItemOrThrow(UUID cartItemId, Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!cartItem.getCart().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        return cartItem;
    }

    // 장바구니 response
    private CartResponseDto buildCartResponse(Cart cart, Boolean storeReplaced) {
        // 항목들의 menuId를 모아 한 번에 조회 (N+1 방지)
        List<UUID> menuIds = cart.getItems().stream()
                .map(CartItem::getMenuId)
                .toList();
        Map<UUID, MenuInfo> menuInfos = menuQueryService.getMenuInfos(menuIds);

        // 병합 + 합계 (삭제된 메뉴는 Map에 없으므로 스킵)
        List<CartResponseDto.Item> items = new ArrayList<>();
        int totalQuantity = 0;
        int totalMenuPrice = 0;
        for (CartItem cartItem : cart.getItems()) {
            MenuInfo menu = menuInfos.get(cartItem.getMenuId());
            if (menu == null) continue;
            CartResponseDto.Item item = CartResponseDto.Item.from(cartItem, menu);
            items.add(item);
            totalQuantity += item.getQuantity();
            totalMenuPrice += item.getItemTotalPrice();
        }

        // Store 정보: 장바구니에 가게가 지정돼 있으면 이름, 최소주문금액 조회 (빈 카트면 null 유지)
        String storeName = null;
        Integer minOrderPrice = null;
        if (cart.getStoreId() != null) {
            Store store = storeRepository.findById(cart.getStoreId()).orElse(null);
            if (store != null) {
                storeName = store.getName();
                minOrderPrice = store.getMinOrderPrice();
            }
        }

        return new CartResponseDto(cart.getId(), storeReplaced, cart.getStoreId(),
                storeName, minOrderPrice, items, totalQuantity, totalMenuPrice);
    }

    // 장바구니 없는 유저 response
    private CartResponseDto emptyCartResponse() {
        return new CartResponseDto(null, null, null,
                null, null, List.of(), 0, 0);
    }

    // CUSTOMER 권한 검증
    private void validateCustomerAccess(String role) {
        if (Role.valueOf(role) != Role.CUSTOMER) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}