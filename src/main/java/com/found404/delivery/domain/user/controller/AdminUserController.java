package com.found404.delivery.domain.user.controller;

import com.found404.delivery.domain.user.dto.UserListResponseDto;
import com.found404.delivery.domain.user.dto.UserResponseDto;
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.service.UserService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// MANAGER, MASTER 둘 다 접근 가능한 유저 관리 기능.
@Tag(name = "Admin - User", description = "관리자용 유저 조회 API (MANAGER, MASTER)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "유저 목록 검색", description = "username/nickname/email/phone 키워드, role 필터, 페이지네이션 지원.")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<UserListResponseDto>> searchUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        UserListResponseDto response = userService.searchUsers(
                userDetails.getRole(), keyword, role, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 특정 유저 단건 조회
    @Operation(summary = "특정 유저 단건 조회", description = "role 상관없이 특정 userId의 유저 상세 정보를 조회한다.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        UserResponseDto response = userService.getUserByAdmin(userDetails.getRole(), userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}