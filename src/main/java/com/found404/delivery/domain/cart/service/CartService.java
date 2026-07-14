package com.found404.delivery.domain.cart.service;

import com.found404.delivery.domain.cart.dto.CartResponseDto;
import com.found404.delivery.domain.cart.entity.Cart;
import com.found404.delivery.domain.cart.repository.CartRepository;
import com.found404.delivery.domain.cartitem.entity.CartItem;
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
    private final MenuQueryService menuQueryService;

    @Transactional(readOnly = true)
    public CartResponseDto getCart(Long userId, String role) {
        validateCustomerAccess(role);
        return cartRepository.findByUserId(userId)
                .map(cart -> buildCartResponse(cart, null))
                .orElseGet(this::emptyCartResponse);
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

    // CUSTOMER 권한 TEMP
    // TODO: UserRole enum 연동 시 교체
    private void validateCustomerAccess(String role) {
        if (!"CUSTOMER".equals(role)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

}
