package com.found404.delivery.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "메뉴 수정 요청 (부분 수정: 변경할 필드만 전송)")
public class MenuUpdateRequestDto {

    @Schema(description = "메뉴명 (변경 시에만, 100자 이하)", example = "고추바사삭")
    @Size(max = 100, message = "메뉴명은 100자 이하여야 합니다.")
    private String name;

    @Schema(description = "가격 (변경 시에만, 0 이상)", example = "21000")
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    @Schema(description = "메뉴 설명 (변경 시에만)", example = "바삭함을 한층 업그레이드")
    private String description;

    @Schema(description = "정렬 순서 (변경 시에만, 0 이상)", example = "2")
    @PositiveOrZero(message = "정렬 순서는 0 이상이어야 합니다.")
    private Integer displayOrder;

    @Schema(description = "AI 설명 생성 여부 (변경 시에만)", example = "true")
    private Boolean aiGenerated;

    @Schema(description = "true면 기존 이미지 제거", example = "false")
    private Boolean removeImage; // true: 기존 이미지 제거
}