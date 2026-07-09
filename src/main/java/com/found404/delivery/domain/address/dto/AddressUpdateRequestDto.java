package com.found404.delivery.domain.address.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressUpdateRequestDto {

    @Size(max = 50, message = "주소 별칭은 50자 이하여야 합니다.")
    private String addressName;

    @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
    private String address;

    @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다.")
    private String detailAddress;

    @Size(max = 50, message = "수령인 이름은 50자 이하여야 합니다.")
    private String receiverName;

    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String phone;
}
