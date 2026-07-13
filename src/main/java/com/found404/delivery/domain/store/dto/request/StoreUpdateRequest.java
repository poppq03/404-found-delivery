package com.found404.delivery.domain.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreUpdateRequest {

    @NotBlank
    private String name;    // Store name

    @NotNull
    private Long categoryId;   // 카테고리 id

    @NotBlank
    private String phoneNumber;   // 매장 전화번호

    @NotBlank
    private String address; //주소

    @NotBlank
    private String detailAddress;


    private String description; // 설명


    private Integer minOrderPrice; //최소 주문금액


    private Integer deliveryFee; //배달비

    @NotBlank
    private String updateBy; // 생성자


    @NotBlank
    private String deletedBy; //삭제자

}
