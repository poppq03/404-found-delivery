package com.found404.delivery.domain.region.controller;

import com.found404.delivery.domain.region.dto.RegionCreateRequestDto;
import com.found404.delivery.domain.region.dto.RegionResponseDto;
import com.found404.delivery.domain.region.service.RegionService;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/regions")
public class RegionController {
    private final RegionService regionService;


    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public RegionResponseDto createRegion(@Valid@RequestBody RegionCreateRequestDto request) {
        return regionService.createRegion(request);
    }


    @DeleteMapping("/{regionId}")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public RegionResponseDto deleteRegion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID regionId
            ) {
        return regionService.deleteRegion(userDetails.getUserId(),regionId);
    }




}
