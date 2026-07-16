package com.found404.delivery.domain.user.controller;

import com.found404.delivery.domain.user.dto.ManagerCreateRequestDto;
import com.found404.delivery.domain.user.dto.SignupResponseDto;
import com.found404.delivery.domain.user.dto.UserResponseDto;
import com.found404.delivery.domain.user.dto.UserUpdateRequestDto;
import com.found404.delivery.domain.user.service.UserService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// MASTER 전용 기능.
// (Admin{도메인}Controller는 MANAGER/MASTER 공용, {도메인}MasterController는 MASTER 전용).
@Tag(name = "Master - Manager", description = "MASTER 전용 MANAGER 계정 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class UserMasterController {

    private final UserService userService;

    @Operation(summary = "MANAGER 계정 생성", description = "role이 MANAGER로 고정되어 생성된다. MASTER만 가능.")
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

    // MANAGER 전용 단건 조회.
    @Operation(summary = "MANAGER 단건 조회", description = "대상이 실제 MANAGER가 아니면 404. MASTER만 가능.")
    @GetMapping("/managers/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getManager(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId
    ) {
        UserResponseDto response = userService.getManagerByAdmin(userDetails.getRole(), userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // MANAGER 계정 정보 수정 (닉네임/전화번호/프로필이미지)
    @Operation(summary = "MANAGER 정보 수정", description = "대상이 실제 MANAGER가 아니면 404. MASTER만 가능.")
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
    @Operation(summary = "MANAGER 삭제", description = "Soft Delete 처리. deletedBy는 삭제 대상이 아닌 요청자(MASTER)로 기록됨. MASTER만 가능.")
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