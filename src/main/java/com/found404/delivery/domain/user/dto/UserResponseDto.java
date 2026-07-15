package com.found404.delivery.domain.user.dto;

import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto { // 내 정보

    private final Long userId;
    private final String username;
    private final String email;
    private final String nickname;
    private final String phone;
    private final String profileImage;
    private final Role role;
    private final LocalDateTime createdAt;

    public UserResponseDto(Long userId, String username, String email, String nickname,
                           String phone, String profileImage, Role role, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.phone = phone;
        this.profileImage = profileImage;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getNickname(),
                user.getPhone(),
                user.getProfileImage(),
                user.getRole(),
                user.getCreatedAt());
    }
}