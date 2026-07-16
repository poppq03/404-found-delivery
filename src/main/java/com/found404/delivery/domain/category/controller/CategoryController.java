package com.found404.delivery.domain.category.controller;

import com.found404.delivery.domain.category.dto.CategoryCreateRequestDto;
import com.found404.delivery.domain.category.dto.CategoryResponseDto;
import com.found404.delivery.domain.category.dto.CategoryUpdateRequestDto;
import com.found404.delivery.domain.category.service.CategoryService;
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
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
            @Valid @RequestBody CategoryCreateRequestDto request
    ) {
        CategoryResponseDto response = categoryService.createCategory(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "카테고리가 등록되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryResponseDto>>> searchCategories(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<CategoryResponseDto> response = categoryService.searchCategories(name, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategory(
            @PathVariable UUID categoryId
    ) {
        CategoryResponseDto response = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody CategoryUpdateRequestDto request
    ) {
        CategoryResponseDto response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('MASTER', 'MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponseDto>> deleteCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID categoryId
    ) {
        CategoryResponseDto response = categoryService.deleteCategory(userDetails.getUserId(), categoryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}