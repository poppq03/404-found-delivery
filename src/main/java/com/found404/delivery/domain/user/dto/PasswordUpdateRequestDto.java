package com.found404.delivery.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordUpdateRequestDto {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    // 회원가입 때와 동일한 규칙(8~15자, 대소문자+숫자+특수문자)을 그대로 적용
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,15}$",
            message = "새 비밀번호는 8자 이상 15자 이하이며 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String newPassword;
}