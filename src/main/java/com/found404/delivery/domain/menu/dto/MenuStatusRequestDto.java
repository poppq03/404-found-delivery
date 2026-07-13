package com.found404.delivery.domain.menu.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuStatusRequestDto {

    private Boolean hidden;
    private Boolean soldOut;

    @AssertTrue(message = "숨김 또는 품절 중 하나 이상을 입력해야 합니다.")
    private boolean isAnyStatusPresent() {
        return hidden != null || soldOut != null;
    }
}
