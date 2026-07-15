package com.found404.delivery.domain.store.dto.response;

import com.found404.delivery.domain.store.entity.StoreStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class StoreStatusResponseDto {

    private UUID storeId;
    @NotNull
    private StoreStatus status;
    private String message;

}
