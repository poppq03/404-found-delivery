package com.found404.delivery.domain.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MinOrderPriceUpdateRequest {

    @NotNull
    @Min(0)
    private Integer minOrderPrice;
}
