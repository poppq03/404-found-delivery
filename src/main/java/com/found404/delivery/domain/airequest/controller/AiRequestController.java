package com.found404.delivery.domain.airequest.controller;

import com.found404.delivery.domain.airequest.dto.AiDescriptionRequestDto;
import com.found404.delivery.domain.airequest.dto.AiDescriptionResponseDto;
import com.found404.delivery.domain.airequest.dto.AiRequestListResponseDto;
import com.found404.delivery.domain.airequest.entity.AiRequestStatus;
import com.found404.delivery.domain.airequest.service.AiRequestService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<AiRequestListResponseDto>> getRequests(
            @RequestParam(required = false) UUID menuId,
            @RequestParam(required = false) AiRequestStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        AiRequestListResponseDto response = aiRequestService.getRequests(
                userDetails.getUserId(), userDetails.getRole(), menuId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
