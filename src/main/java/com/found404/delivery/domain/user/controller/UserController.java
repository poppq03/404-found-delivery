package com.found404.delivery.domain.user.controller;

import com.found404.delivery.domain.user.dto.SignupRequestDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(
            @Valid @RequestBody SignupRequestDto request
    ) {
        SignupResponseDto response = userService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "회원가입이 완료되었습니다."));
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponseDto response = userService.getMyInfo(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 내 정보(닉네임/전화번호/프로필이미지) 수정
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {
        UserResponseDto response = userService.updateMyInfo(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "회원 정보가 수정되었습니다."));
    }
}