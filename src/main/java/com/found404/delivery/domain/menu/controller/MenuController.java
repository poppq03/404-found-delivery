package com.found404.delivery.domain.menu.controller;

import com.found404.delivery.domain.menu.dto.*;
import com.found404.delivery.domain.menu.service.MenuService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Menu", description = "메뉴 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "메뉴 등록", description = "사장님이 본인 가게에 메뉴를 등록합니다.")
    @PostMapping(value = "/stores/{storeId}/menus", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MenuCreateResponseDto>> createMenu(
            @Parameter(description = "가게 ID") @PathVariable UUID storeId,
            @Valid @RequestPart("data") MenuCreateRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        MenuCreateResponseDto response = menuService.createMenu(
                storeId, userDetails.getUserId(), userDetails.getRole(), request, image);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 상세 조회", description = "메뉴 단건 상세를 조회합니다. 숨김 메뉴는 조회 권한이 있어야 보입니다.")
    @GetMapping("/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuDetailResponseDto>> getMenuDetails(
            @Parameter(description = "메뉴 ID") @PathVariable UUID menuId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        String role = (userDetails != null) ? userDetails.getRole() : null;

        MenuDetailResponseDto response = menuService.getMenu(menuId, userId, role);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 목록 조회", description = "가게의 메뉴 목록을 검색·페이징 조회합니다. (키워드, 품절 필터, 정렬 지원)")
    @GetMapping("/stores/{storeId}/menus")
    public ResponseEntity<ApiResponse<MenuListResponseDto>> getMenuLists(
            @Parameter(description = "가게 ID") @PathVariable UUID storeId,
            @Parameter(description = "메뉴명 검색어") @RequestParam(required = false) String keyword,
            @Parameter(description = "품절 필터 (true=품절만)") @RequestParam(required = false) Boolean soldOut,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Long userId = (userDetails != null) ? userDetails.getUserId() : null;
        String role = (userDetails != null) ? userDetails.getRole() : null;

        MenuListResponseDto response = menuService.getMenus(
                storeId, keyword, soldOut, userId, role, pageable);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 수정", description = "사장님이 본인 가게 메뉴를 수정합니다.")
    @PutMapping(value = "/menus/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<MenuUpdateResponseDto>> updateMenu(
            @Parameter(description = "메뉴 ID") @PathVariable UUID menuId,
            @Valid @RequestPart("data") MenuUpdateRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        MenuUpdateResponseDto response = menuService.updateMenu(
                menuId, userDetails.getUserId(), userDetails.getRole(), request, image);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 숨김/품절 상태 변경", description = "사장님이 메뉴의 숨김·품절 상태를 변경합니다.")
    @PatchMapping("/menus/{menuId}/status")
    public ResponseEntity<ApiResponse<MenuStatusResponseDto>> changeStatus(
            @Parameter(description = "메뉴 ID") @PathVariable UUID menuId,
            @Valid @RequestBody MenuStatusRequestDto request,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        MenuStatusResponseDto response = menuService.changeStatus(
                menuId, userDetails.getUserId(), userDetails.getRole(), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "메뉴 삭제", description = "사장님이 본인 가게 메뉴를 삭제합니다.")
    @DeleteMapping("/menus/{menuId}")
    public ResponseEntity<ApiResponse<MenuDeleteResponseDto>> deleteMenu(
            @Parameter(description = "메뉴 ID") @PathVariable UUID menuId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        MenuDeleteResponseDto response = menuService.deleteMenu(menuId, userDetails.getUserId(), userDetails.getRole());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}