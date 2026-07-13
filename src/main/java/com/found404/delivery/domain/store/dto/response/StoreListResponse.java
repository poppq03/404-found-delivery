package com.found404.delivery.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoreListResponse {

    private List<StoreSimpleResponse> stores;

}
