package com.found404.delivery.domain.airequest.controller;

import com.found404.delivery.domain.airequest.dto.AiDescriptionRequestDto;
import com.found404.delivery.domain.airequest.dto.AiDescriptionResponseDto;
import com.found404.delivery.domain.airequest.dto.AiRequestListResponseDto;
import com.found404.delivery.domain.airequest.entity.AiRequestStatus;
import com.found404.delivery.domain.airequest.service.AiRequestService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "AI", description = "AI 메뉴 설명 API")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiRequestController {

    private final AiRequestService aiRequestService;

    @Operation(summary = "AI 메뉴 설명 생성",
            description = "프롬프트로 메뉴 설명을 생성합니다. 요청/응답은 로그로 저장되며, 실패해도 FAILED로 기록됩니다.")
    @PostMapping("/menu-descriptions")
    public ResponseEntity<ApiResponse<AiDescriptionResponseDto>> generateDescription(
            @Valid @RequestBody AiDescriptionRequestDto request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        AiDescriptionResponseDto response = aiRequestService.generateDescription(
                request, userDetails.getUserId(), userDetails.getRole());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "AI 요청 이력 조회",
            description = "AI 요청/응답 이력을 검색·페이징 조회합니다.")
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<AiRequestListResponseDto>> getRequests(
            @Parameter(description = "메뉴 ID 필터") @RequestParam(required = false) UUID menuId,
            @Parameter(description = "상태 필터 (SUCCESS/FAILED)") @RequestParam(required = false) AiRequestStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        AiRequestListResponseDto response = aiRequestService.getRequests(
                userDetails.getUserId(), userDetails.getRole(), menuId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}