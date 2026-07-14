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

        // 메뉴 검증
        MenuInfo menu = menuQueryService.getMenuInfo(request.getMenuId());
        if (menu.isHidden() || menu.isSoldOut()) {
            throw new CustomException(ErrorCode.MENU_UNAVAILABLE);
        }

        // 수량 검증
        if (request.getQuantity() < 1) {
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }

        Cart cart = getOrCreateCart(userId);

        // 장바구니 교체 (인당 한 가게에 대한 장바구니만)
        boolean storeReplaced = false;
        if (cart.getStoreId() != null && !cart.getStoreId().equals(menu.storeId())) {
            cart.clearCart();
            storeReplaced = true;
        }

        cart.addItem(menu.menuId(), request.getQuantity(), menu.storeId());

        return buildCartResponse(cart, storeReplaced);
    }

    @Transactional
    public CartResponseDto changeQuantity(Long userId, String role, UUID cartItemId, CartItemQuantityRequestDto request) {

        validateCustomerAccess(role);

        // 수량 검증
        if (request.getQuantity() < 1) {
            throw new CustomException(ErrorCode.INVALID_QUANTITY);
        }

        // 본인 장바구니 확인
        CartItem cartItem = getCartItemOrThrow(cartItemId, userId);

        cartItem.changeQuantity(request.getQuantity());

        return buildCartResponse(cartItem.getCart(), null);
    }

    private CartItem getCartItemOrThrow(UUID cartItemId, Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (!cartItem.getCart().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        return cartItem;
    }
    private CartResponseDto buildCartResponse(Cart cart, Boolean storeReplaced) {
        List<UUID> menuIds = cart.getItems().stream()
                .map(CartItem::getMenuId)
                .toList();
        Map<UUID, MenuInfo> menuInfos = menuQueryService.getMenuInfos(menuIds);

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

        // Store 정보 [TEMP] TODO: store 연동 후 교체
        String storeName = null;
        Integer minOrderPrice = null;

        return new CartResponseDto(cart.getId(), storeReplaced, cart.getStoreId(),
                storeName, minOrderPrice, items, totalQuantity, totalMenuPrice);
    }

    private CartResponseDto emptyCartResponse() {
        return new CartResponseDto(null, null, null,
                null, null, List.of(), 0, 0);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().userId(userId).build()));
    }

    // CUSTOMER 권한 TEMP
    // TODO: UserRole enum 연동 시 교체
    private void validateCustomerAccess(String role) {
        if (!"CUSTOMER".equals(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

}
