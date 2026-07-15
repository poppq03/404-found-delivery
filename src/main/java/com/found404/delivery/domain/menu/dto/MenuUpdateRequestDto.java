package com.found404.delivery.domain.menu.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MenuUpdateRequestDto {

    @Size(max = 100, message = "메뉴명은 100자 이하여야 합니다.")
    private String name;

    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    private String description;

    @PositiveOrZero(message = "정렬 순서는 0 이상이어야 합니다.")
    private Integer displayOrder;

    private Boolean aiGenerated;

    private Boolean removeImage; // true: 기존 이미지 제거
}
