package com.found404.delivery.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "주문 거절 요청 DTO")
@Getter
@NoArgsConstructor
public class OrderRejectRequestDto {

    @Schema(description = "주문 거절 사유", example = "재료 소진으로 주문을 받을 수 없습니다.")
    @Size(max = 255)
    private String reason;
}
