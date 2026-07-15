package com.found404.delivery.domain.user.controller;

import com.found404.delivery.domain.user.dto.ManagerCreateRequestDto;
import com.found404.delivery.domain.user.dto.SignupResponseDto;
import com.found404.delivery.domain.user.dto.UserResponseDto;
import com.found404.delivery.domain.user.dto.UserUpdateRequestDto;
import com.found404.delivery.domain.user.service.UserService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// MASTER 전용 기능. StoreMasterController와 동일한 네이밍 규칙 적용
// (Admin{도메인}Controller는 MANAGER/MASTER 공용, {도메인}MasterController는 MASTER 전용).
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class UserMasterController {

    private final UserService userService;

    @PostMapping("/managers")
    public ResponseEntity<ApiResponse<SignupResponseDto>> createManager(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ManagerCreateRequestDto request
    ) {
        SignupResponseDto response = userService.createManager(userDetails.getRole(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "MANAGER 계정이 생성되었습니다."));
    }

    // MANAGER 계정 정보 수정 (닉네임/전화번호/프로필이미지)
    @PatchMapping("/managers/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateManager(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        UserResponseDto response = userService.updateManager(userDetails.getRole(), userId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "MANAGER 정보가 수정되었습니다."));
    }

    // MANAGER 계정 삭제 (Soft Delete)
    @DeleteMapping("/managers/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteManager(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        String deletedUsername = userService.deleteManager(userDetails.getRole(), userDetails.getUserId(), userId);

        String message = deletedUsername + " MANAGER가 삭제되었습니다.";
        return ResponseEntity.ok(ApiResponse.success(null, message));
    }
}