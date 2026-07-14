package com.found404.delivery.domain.store.dto.response;

import com.found404.delivery.domain.store.entity.StoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class StoreStatusResponseDto {

    private UUID storeId;
    private StoreStatus status;
    private String message;

}
