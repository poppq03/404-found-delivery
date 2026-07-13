package com.found404.delivery.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StoreSimpleResponse {

    private UUID storeId;

    private String name;

    private String categoryId;

    private Integer deliveryFee;

    private Integer minOrderPrice;

    private String imageUrl;

    //가게 목록
}
