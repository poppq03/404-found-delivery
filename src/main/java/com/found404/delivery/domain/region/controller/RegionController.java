package com.found404.delivery.domain.region.controller;

import com.found404.delivery.domain.region.dto.RegionCreateRequestDto;
import com.found404.delivery.domain.region.dto.RegionResponseDto;
import com.found404.delivery.domain.region.service.RegionService;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/regions")
@Tag(name = "Region", description = "MANAGER | MASTER Region 생성 API")
public class RegionController {
    private final RegionService regionService;


    @Operation(summary = "createRegion", description = "api/v1/regions")
    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public RegionResponseDto createRegion(@Valid@RequestBody RegionCreateRequestDto request) {
        return regionService.createRegion(request);
    }

    @Operation(summary = "deleteRegion", description = "api/v1/regions/{regionId}")
    @DeleteMapping("/{regionId}")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public RegionResponseDto deleteRegion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID regionId
            ) {
        return regionService.deleteRegion(userDetails.getUserId(),regionId);
    }



}
