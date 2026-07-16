package com.found404.delivery.domain.airequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "AI 메뉴 설명 생성 요청")
public class AiDescriptionRequestDto {

    @Schema(description = "AI 생성용 프롬프트 (최대 200자)", example = "매운 양념치킨을 먹음직스럽게 소개해줘")
    @NotBlank(message = "프롬프트 입력은 필수입니다.")
    @Size(max = 200, message = "프롬프트 내용은 200자 이내로 입력해 주세요.")
    private String prompt;

    @Schema(description = "요청이 발생한 가게 ID (로그 기록용, 선택)", example = "8b1c2d3e-4f5a-6b7c-8d9e-0f1a2b3c4d5e")
    private UUID storeId;

    @Schema(description = "기존 메뉴 재생성 시 로그 연결용 메뉴 ID (선택)", example = "c22d3e4f-5a6b-7c8d-9e0f-1a2b3c4d5e6f")
    private UUID menuId;
}