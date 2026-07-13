package com.found404.delivery.domain.airequest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class AiDescriptionRequestDto {

    @NotBlank(message = "프롬프트 입력은 필수입니다.")
    @Size(max = 200, message = "프롬프트 내용은 200자 이내로 입력해 주세요.")
    private String prompt;

    private UUID storeId;

    private UUID menuId;
}
