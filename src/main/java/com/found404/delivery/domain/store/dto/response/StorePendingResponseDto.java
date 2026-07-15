package com.found404.delivery.domain.store.dto.response;

import com.found404.delivery.domain.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class StorePendingResponseDto {

    private UUID storeId;
    private String storeName;
    private String ownerName;
    private String categoryName;
    private Integer deliveryFee;
    private Integer minOrderPrice;
    private LocalDateTime createdAt;


    public static StorePendingResponseDto from(Store store) {
        return StorePendingResponseDto.builder()
                .storeId(store.getStoreId())
                .storeName(store.getName())
                .ownerName(store.getOwnerId().getNickname())
                .categoryName(store.getCategory().getName())
                .deliveryFee(store.getDeliveryFee())
                .minOrderPrice(store.getMinOrderPrice())
                .createdAt(store.getCreatedAt())
                .build();
    }


}
