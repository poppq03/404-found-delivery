package com.found404.delivery.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class StoreCreateRequestDto {
    // UUID 값은 자동생성이니 필요 X

    @Schema(description = "가게 이름")
    @NotBlank
    private String name;    // Store name

    @Schema(description = "카테고리 id")
    @NotNull
    private UUID categoryId;   // 카테고리 id

    @Schema(description = "지역 id")
    @NotNull
    private UUID regionId;

    @Schema(description = "전화번호")
    @NotBlank
    private String phoneNumber;   // 매장 전화번호

    @Schema(description = "주소")
    @NotBlank
    private String address; //주소

    @Schema(description = "상세 주소")
    @NotBlank
    private String detailAddress;

    @Schema(description = "가게 설명")
    private String description; // 설명

    @Schema(description = "최소 주문 금액")
    private Integer minOrderPrice; //최소 주문금액

    @Schema(description = "배달비")
    private Integer deliveryFee; //배달비

}
