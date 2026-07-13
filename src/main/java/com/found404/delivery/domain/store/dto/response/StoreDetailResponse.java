package com.found404.delivery.domain.store.dto.response;

import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.entity.StoreStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StoreDetailResponse {  //가게 상세 조회

    private UUID storeId;

    private String name;

    private String phoneNumber;

    private StoreStatus storeStatus;

    private String description;

    private String address;

    private String detailAddress;

    private Integer minOrderPrice;

    private Integer deliveryFee;

    private String imageUrl;

    public static StoreDetailResponse from(Store store) {

        return StoreDetailResponse.builder()
                .storeId(store.getStoreId())
                .name(store.getName())
                .description(store.getDescription())
                .phoneNumber(store.getPhoneNumber())
                .address(store.getAddress())
                .detailAddress(store.getDetailAddress())
                .minOrderPrice(store.getMinOrderPrice())
                .deliveryFee(store.getDeliveryFee())
                .imageUrl(store.getImageUrl())
                .storeStatus(store.getStatus())
                .build();
    }
}
