package com.found404.delivery.domain.menu.controller;

import com.found404.delivery.domain.menu.dto.*;
import com.found404.delivery.domain.menu.service.MenuService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MenuController {

    private final MenuService menuService;

    @PostMapping(value = "/stores/{storeId}/menus", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MenuCreateResponseDto>> createMenu(
            @PathVariable UUID storeId,
            @Valid @RequestPart("data") MenuCreateRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MenuCreateResponseDto response = menuService.createMenu(
                storeId, userDetails.getUserId(), userDetails.getRole(), request, image);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponseDto>> getMenuDetails(
            @PathVariable UUID menuId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MenuDetailResponseDto response = menuService.getMenu(menuId, userDetails.getUserId(), userDetails.getRole());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<MenuListResponseDto>> getMenuLists(
            @PathVariable UUID storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean soldOut,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        MenuListResponseDto response = menuService.getMenus(
                storeId, keyword, soldOut, userDetails.getUserId(), userDetails.getRole(), pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping(value = "/menus/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MenuUpdateResponseDto>> updateMenu(
            @PathVariable UUID menuId,
            @Valid @RequestPart("data") MenuUpdateRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        MenuUpdateResponseDto response = menuService.updateMenu(
                menuId, userDetails.getUserId(), userDetails.getRole(), request, image);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/menus/{menuId}/status")
    public ResponseEntity<ApiResponse<MenuStatusResponseDto>> changeStatus(
            @PathVariable UUID menuId,
            @Valid @RequestBody MenuStatusRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        MenuStatusResponseDto response = menuService.changeStatus(
                menuId, userDetails.getUserId(), userDetails.getRole(), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuDeleteResponseDto>> deleteMenu(
            @PathVariable UUID menuId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        MenuDeleteResponseDto response = menuService.deleteMenu(menuId, userDetails.getUserId(), userDetails.getRole());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}