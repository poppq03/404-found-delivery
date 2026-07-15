package com.found404.delivery.domain.user.controller;

import com.found404.delivery.domain.user.dto.UserListResponseDto;
import com.found404.delivery.domain.user.entity.Role;
import com.found404.delivery.domain.user.service.UserService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// MANAGER, MASTER 둘 다 접근 가능한 유저 관리 기능. 팀 컨벤션(Admin{도메인}Controller) 적용.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final UserService userService;

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
}