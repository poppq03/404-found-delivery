package com.found404.delivery.domain.user.dto;

import com.found404.delivery.domain.user.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDto {

    @NotBlank(message = "username은 필수입니다.")
    @Pattern(
            regexp = "^[a-z0-9]{4,10}$",
            message = "username은 4자 이상 10자 이하의 영문 소문자와 숫자로만 구성되어야 합니다."
    )
    private String username;

    @NotBlank(message = "password는 필수입니다.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,15}$",
            message = "password는 8자 이상 15자 이하이며 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "email은 필수입니다.")
    @Email(message = "email 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "email은 255자 이하여야 합니다.")
    private String email;

    @NotBlank(message = "nickname은 필수입니다.")
    @Size(max = 50, message = "nickname은 50자 이하여야 합니다.")
    private String nickname;

    @NotBlank(message = "phone은 필수입니다.")
    @Size(max = 50, message = "phone은 50자 이하여야 합니다.")
    private String phone;

    @NotNull(message = "role은 필수입니다.")
    private Role role;
}