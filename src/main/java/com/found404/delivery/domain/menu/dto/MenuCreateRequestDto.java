package com.found404.delivery.domain.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "메뉴 등록 요청")
public class MenuCreateRequestDto {

    @Schema(description = "메뉴명 (최대 100자)", example = "불고기 버거")
    @NotBlank(message = "메뉴명은 필수입니다.")
    @Size(max = 100, message = "메뉴명은 100자 이하여야 합니다.")
    private String name;

    @Schema(description = "가격 (원, 0 이상)", example = "20000")
    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    @Schema(description = "메뉴 설명 (선택, AI 생성 가능)", example = "직화 풍미의 불고기 패티가 들어간 든든한 버거")
    private String description;

    @Schema(description = "가게 내 정렬 순서 (선택, 0 이상)", example = "1")
    @PositiveOrZero(message = "정렬 순서는 0 이상이어야 합니다.")
    private Integer displayOrder;

    @Schema(description = "AI 설명 생성 여부 (선택)", example = "false")
    private Boolean aiGenerated;
}