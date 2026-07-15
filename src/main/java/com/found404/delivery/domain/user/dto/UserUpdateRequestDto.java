package com.found404.delivery.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserUpdateRequestDto {

    // 전부 선택 입력
    @Size(max = 50, message = "nickname은 50자 이하여야 합니다.")
    private String nickname;

    @Size(max = 50, message = "phone은 50자 이하여야 합니다.")
    private String phone;

    @Size(max = 255, message = "profileImage는 255자 이하여야 합니다.")
    private String profileImage;
}