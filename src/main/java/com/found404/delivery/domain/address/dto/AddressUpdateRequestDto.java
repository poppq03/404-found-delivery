package com.found404.delivery.domain.address.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "배송지 수정 요청 DTO")
@Getter
@NoArgsConstructor
public class AddressUpdateRequestDto {

    @Schema(description = "주소 별칭", example = "회사")
    @Size(max = 50, message = "주소 별칭은 50자 이하여야 합니다.")
    private String addressName;

    @Schema(description = "주소", example = "서울특별시 서초구 서초대로 456")
    @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
    private String address;

    @Schema(description = "상세 주소", example = "202호")
    @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다.")
    private String detailAddress;

    @Schema(description = "수령인 이름", example = "홍길동")
    @Size(max = 50, message = "수령인 이름은 50자 이하여야 합니다.")
    private String receiverName;

    @Schema(description = "수령인 연락처", example = "010-9876-5432")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String phone;
}
