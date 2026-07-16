package com.found404.delivery.domain.user.dto;

import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SignupResponseDto {

    private final Long userId;
    private final String username;
    private final String email;
    private final String nickname;
    private final Role role;
    private final LocalDateTime createdAt;

    public SignupResponseDto(Long userId, String username, String email,
                             String nickname, Role role, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static SignupResponseDto from(User user) {
        return new SignupResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getRole(),
                user.getCreatedAt());
    }
}