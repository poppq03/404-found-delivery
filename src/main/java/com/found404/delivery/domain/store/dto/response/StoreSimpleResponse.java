package com.found404.delivery.domain.store.dto.response;

import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.entity.StoreStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StoreSimpleResponse {

    private UUID storeId;

    private String name;

    private UUID categoryId;

    private Integer deliveryFee;

    private Integer minOrderPrice;

    private String imageUrl;

    private StoreStatus storeStatus;

    //가게 목록

    public static StoreSimpleResponse from (Store store) {
        return StoreSimpleResponse.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .deliveryFee(store.getDeliveryFee())
                .minOrderPrice(store.getMinOrderPrice())
                .imageUrl(store.getImageUrl())
                .storeStatus(store.getStatus())
                .build();
    }

}
