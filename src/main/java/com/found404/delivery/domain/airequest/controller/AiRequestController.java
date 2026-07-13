package com.found404.delivery.domain.airequest.controller;

import com.found404.delivery.domain.airequest.dto.AiDescriptionRequestDto;
import com.found404.delivery.domain.airequest.dto.AiDescriptionResponseDto;
import com.found404.delivery.domain.airequest.service.AiRequestService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiRequestController {

    private final AiRequestService aiRequestService;

    @PostMapping("/menu-descriptions")
    public ResponseEntity<ApiResponse<AiDescriptionResponseDto>> generateDescription(
            @Valid @RequestBody AiDescriptionRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        AiDescriptionResponseDto response = aiRequestService.generateDescription(
                request, userDetails.getUserId(), userDetails.getRole());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
