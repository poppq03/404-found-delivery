package com.found404.delivery.domain.store.dto.response;

import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.entity.StoreStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StoreSimpleResponseDto {

    private UUID storeId;

    private String name;

    private UUID categoryId;

    private Integer deliveryFee;

    private Integer minOrderPrice;

    private String imageUrl;

    private StoreStatus storeStatus;

    //가게 목록

    public static StoreSimpleResponseDto from(Store store, String imageUrl) {
        return StoreSimpleResponseDto.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .categoryId(store.getCategory().getCategoryId())   // 추가
                .deliveryFee(store.getDeliveryFee())
                .minOrderPrice(store.getMinOrderPrice())
                .imageUrl(imageUrl)
                .storeStatus(store.getStatus())
                .build();
    }
}
