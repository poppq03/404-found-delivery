package com.found404.delivery.domain.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MinOrderPriceUpdateRequestDto {

    @NotNull(message = "최소 주문 금액은 필수입니다.")
    @PositiveOrZero(message = "최소 주문 금액은 0원 이상이어야 합니다.")
    private Integer minOrderPrice;
}
