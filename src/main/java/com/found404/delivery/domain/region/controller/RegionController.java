package com.found404.delivery.domain.region.controller;

import com.found404.delivery.domain.region.dto.RegionCreateRequestDto;
import com.found404.delivery.domain.region.dto.RegionResponseDto;
import com.found404.delivery.domain.region.dto.RegionUpdateRequestDto;
import com.found404.delivery.domain.region.service.RegionService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/regions")
public class RegionController {

    private final RegionService regionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ResponseEntity<ApiResponse<RegionResponseDto>> createRegion(
            @Valid @RequestBody RegionCreateRequestDto request
    ) {
        RegionResponseDto response = regionService.createRegion(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "지역이 등록되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RegionResponseDto>>> searchRegions(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<RegionResponseDto> response = regionService.searchRegions(name, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{regionId}")
    public ResponseEntity<ApiResponse<RegionResponseDto>> getRegion(
            @PathVariable UUID regionId
    ) {
        RegionResponseDto response = regionService.getRegion(regionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{regionId}")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ResponseEntity<ApiResponse<RegionResponseDto>> updateRegion(
            @PathVariable UUID regionId,
            @Valid @RequestBody RegionUpdateRequestDto request
    ) {
        RegionResponseDto response = regionService.updateRegion(regionId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{regionId}")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ResponseEntity<ApiResponse<RegionResponseDto>> deleteRegion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID regionId
    ) {
        RegionResponseDto response = regionService.deleteRegion(userDetails.getUserId(), regionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}