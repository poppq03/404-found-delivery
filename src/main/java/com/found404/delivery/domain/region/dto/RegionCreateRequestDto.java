package com.found404.delivery.domain.region.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegionCreateRequestDto {

    @NotBlank(message = "지역 이름은 필수입니다.")
    @Size(max = 100, message = "지역 이름은 100자 이하로 입력해주세요.")
    private String name;
}