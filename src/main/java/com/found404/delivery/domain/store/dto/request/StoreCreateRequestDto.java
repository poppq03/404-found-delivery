package com.found404.delivery.domain.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class StoreCreateRequestDto {
    // UUID 값은 자동생성이니 필요 X

    @NotBlank
    private String name;    // Store name

    @NotNull
    private UUID categoryId;   // 카테고리 id

    @NotNull
    private UUID regionId;

    @NotBlank
    private String phoneNumber;   // 매장 전화번호

    @NotBlank
    private String address; //주소

    @NotBlank
    private String detailAddress;


    private String description; // 설명


    private Integer minOrderPrice; //최소 주문금액


    private Integer deliveryFee; //배달비

    private String imageUrl; //이미지 주소

}
