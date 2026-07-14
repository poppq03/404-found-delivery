package com.found404.delivery.domain.store.dto.request;

import com.found404.delivery.domain.store.entity.StoreStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreStatusRequestDto {

    private StoreStatus status;


}
