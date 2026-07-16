package com.found404.delivery.domain.user.dto;

import jakarta.validation.constraints.Pattern;
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

    // XSS 방지: http(s) 스킴만 허용
    @Size(max = 255, message = "profileImage는 255자 이하여야 합니다.")
    @Pattern(
            regexp = "^$|^https?://[A-Za-z0-9\\-._~:/?#\\[\\]@!$&'()*+,;=%]+$",
            message = "profileImage는 http(s):// 로 시작하는 안전한 URL 형식이어야 합니다."
    )
    private String profileImage;
}