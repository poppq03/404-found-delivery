package com.found404.delivery.domain.user.dto;

import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserSummaryResponseDto {

    private final Long userId;
    private final String username;
    private final String nickname;
    private final String email;
    private final String phone;
    private final Role role;
    private final LocalDateTime createdAt;

    // 목록용 요약(한 줄)정보 Dto
    public UserSummaryResponseDto(Long userId, String username, String nickname,
                                  String email, String phone, Role role, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static UserSummaryResponseDto from(User user) {
        return new UserSummaryResponseDto(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt());
    }
}