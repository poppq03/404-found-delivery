package com.found404.delivery.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreImageUpdateResponse {
    private String imageUrl;
    private String message;
}
