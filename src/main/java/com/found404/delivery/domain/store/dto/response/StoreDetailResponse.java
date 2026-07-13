package com.found404.delivery.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StoreDetailResponse {  //가게 상세 조회

    private UUID storeId;

    private String name;

    private String phoneNumber;

    private String description;

    private String address;

    private String detailAddress;

    private Integer minOrderPrice;

    private Integer deliveryFee;

    //private String imageUrl;
}
