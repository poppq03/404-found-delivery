package com.found404.delivery.domain.user.controller;

import com.found404.delivery.domain.user.dto.ManagerCreateRequestDto;
import com.found404.delivery.domain.user.dto.SignupResponseDto;
import com.found404.delivery.domain.user.service.UserService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}