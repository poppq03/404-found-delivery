package com.found404.delivery.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "리뷰 생성 요청")
public record ReviewCreateRequest(

        @Schema(description = "리뷰 대상 주문 ID", example = "b1c2d3e4-5f6a-7b8c-9d0e-1f2a3b4c5d6e")
        @NotNull(message = "주문 ID는 필수입니다.")
        UUID orderId,

        @Schema(description = "리뷰 대상 가게 ID", example = "aaaaaaaa-0000-0000-0000-000000000001")
        @NotNull(message = "가게 ID는 필수입니다.")
        UUID storeId,

        @Schema(description = "별점 (1~5)", example = "5")
        @NotNull(message = "별점은 필수입니다.")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
        Integer rating,

        @Schema(description = "리뷰 내용", example = "치킨이 바삭하고 맛있어요!")
        @NotBlank(message = "리뷰 내용은 필수입니다.")
        String content
) {
}