package com.found404.delivery.domain.address.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressRequestDto {

    @Size(max = 50, message = "주소 별칭은 50자 이하여야 합니다.")
    private String addressName;

    @NotBlank(message = "주소는 필수입니다.")
    @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
    private String address;

    @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다.")
    private String detailAddress;

    @NotBlank(message = "수령인 이름은 필수 입니다.")
    @Size(max = 50, message = "수령인 이름은 50자 이하여야 합니다.")
    private String receiverName;

    @NotBlank(message = "수령인 연락처는 필수입니다.")
    @Size(max = 20, message = "전화번호는 20자 이하여야합니다.")
    private String phone;
}
