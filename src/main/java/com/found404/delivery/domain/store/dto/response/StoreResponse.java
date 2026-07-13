package com.found404.delivery.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StoreResponse {

    private UUID storeId;
    private String name;
    private String status;
}
