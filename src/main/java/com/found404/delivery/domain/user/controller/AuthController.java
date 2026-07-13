package com.found404.delivery.domain.user.controller;

import com.found404.delivery.domain.user.dto.LoginRequestDto;
import com.found404.delivery.domain.user.dto.LoginResponseDto;
import com.found404.delivery.domain.user.service.UserService;
import com.found404.delivery.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request
    ) {
        LoginResponseDto response = userService.login(request);
        return ResponseEntity
                .status(HttpStatus.OK) // 회원가입과 다름
                .body(ApiResponse.success(response, "로그인이 완료되었습니다."));
    }
}