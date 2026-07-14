package com.found404.delivery.domain.cart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.found404.delivery.domain.cartitem.entity.CartItem;
import com.found404.delivery.domain.menu.service.MenuInfo;
import lombok.Getter;

import java.util.List;
import java.util.UUID;


@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartResponseDto {

    private final UUID cartId;
    private final Boolean storeReplaced;   // 담기에서의 세팅값
    private final UUID storeId;
    private final String storeName;        // Store 연동 전 [TEMP] → null TODO: 연동 후 확인
    private final Integer minOrderPrice;   // Store 연동 전 [TEMP] → null TODO: 연동 후 확인
    private final List<Item> items;
    private final Integer totalQuantity;
    private final Integer totalMenuPrice;

    public CartResponseDto(UUID cartId, Boolean storeReplaced, UUID storeId,
                           String storeName, Integer minOrderPrice, List<Item> items,
                           Integer totalQuantity, Integer totalMenuPrice) {
        this.cartId = cartId;
        this.storeReplaced = storeReplaced;
        this.storeId = storeId;
        this.storeName = storeName;
        this.minOrderPrice = minOrderPrice;
        this.items = items;
        this.totalQuantity = totalQuantity;
        this.totalMenuPrice = totalMenuPrice;
    }

    // ===== 장바구니 항목 하나 =====
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Item {
        private final UUID cartItemId;
        private final UUID menuId;
        private final String menuName;
        private final int menuPrice;
        private final int quantity;
        private final int itemTotalPrice;
        @JsonProperty("isSoldOut") private final boolean isSoldOut;
        @JsonProperty("isHidden")  private final boolean isHidden;
        private final String imageUrl;

        public Item(UUID cartItemId, UUID menuId, String menuName, int menuPrice, int quantity,
                    int itemTotalPrice, boolean isSoldOut, boolean isHidden, String imageUrl) {
            this.cartItemId = cartItemId;
            this.menuId = menuId;
            this.menuName = menuName;
            this.menuPrice = menuPrice;
            this.quantity = quantity;
            this.itemTotalPrice = itemTotalPrice;
            this.isSoldOut = isSoldOut;
            this.isHidden = isHidden;
            this.imageUrl = imageUrl;
        }

        // CartItem + MenuInfo 필드 매핑용
        public static Item from(CartItem cartItem, MenuInfo menu) {
            int quantity = cartItem.getQuantity();
            return new Item(
                    cartItem.getId(),
                    menu.menuId(),
                    menu.name(),
                    menu.price(),
                    quantity,
                    menu.price() * quantity,
                    menu.isSoldOut(),
                    menu.isHidden(),
                    menu.imageUrl()
            );
        }
    }
}