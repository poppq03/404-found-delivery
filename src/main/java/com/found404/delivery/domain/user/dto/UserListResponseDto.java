package com.found404.delivery.domain.user.dto;

import com.found404.delivery.domain.user.entity.User;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class UserListResponseDto {

    private final List<UserSummaryResponseDto> content;
    private final int page;
    private final int size;
    private final Long totalElements;
    private final int totalPages;

    public UserListResponseDto(List<UserSummaryResponseDto> content, int page, int size,
                               Long totalElements, int totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public static UserListResponseDto from(Page<User> userPage) {
        List<UserSummaryResponseDto> content = userPage.getContent().stream()
                .map(UserSummaryResponseDto::from)
                .toList();

        return new UserListResponseDto(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages());
    }
}