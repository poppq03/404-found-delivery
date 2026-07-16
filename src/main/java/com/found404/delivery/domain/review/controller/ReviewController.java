package com.found404.delivery.domain.review.controller;

import com.found404.delivery.domain.review.dto.ReviewCreateRequest;
import com.found404.delivery.domain.review.dto.ReviewResponse;
import com.found404.delivery.domain.review.dto.ReviewUpdateRequest;
import com.found404.delivery.domain.review.service.ReviewService;
import com.found404.delivery.global.response.ApiResponse;
import com.found404.delivery.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Review", description = "리뷰 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 생성
     */
    @Operation(summary = "리뷰 생성", description = "배송 완료된 주문에 대해 주문자 본인이 리뷰를 작성합니다. (별점 1~5, 주문 1건당 리뷰 1건)")
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewResponse response = reviewService.createReview(
                userDetails.getUserId(),
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 리뷰 단건 조회
     */
    @Operation(summary = "리뷰 단건 조회", description = "리뷰 ID로 리뷰 하나를 조회합니다.")
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(
            @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId
    ) {
        ReviewResponse response =
                reviewService.getReview(reviewId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 가게별 공개 리뷰 목록 조회
     */
    @Operation(summary = "가게별 리뷰 목록 조회", description = "특정 가게의 공개(숨김 아님) 리뷰 목록을 조회합니다.")
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByStoreId(
            @Parameter(description = "가게 ID") @PathVariable UUID storeId
    ) {
        List<ReviewResponse> responses =
                reviewService.getReviewsByStoreId(storeId);

        return ResponseEntity.ok(
                ApiResponse.success(responses)
        );
    }

    /**
     * 리뷰 수정
     */
    @Operation(summary = "리뷰 수정", description = "리뷰 작성자 본인이 별점·내용을 수정합니다.")
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        ReviewResponse response =
                reviewService.updateReview(
                        userDetails.getUserId(),
                        reviewId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 리뷰 숨김 처리
     */
    @Operation(summary = "리뷰 숨김 처리", description = "MANAGER·MASTER 또는 해당 가게를 소유한 OWNER가 리뷰를 숨김 처리합니다.")
    @PatchMapping("/reviews/{reviewId}/hide")
    public ResponseEntity<ApiResponse<ReviewResponse>> hideReview(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId
    ) {
        ReviewResponse response =
                reviewService.hideReview(
                        userDetails.getUserId(),
                        userDetails.getRole(),
                        reviewId
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 리뷰 숨김 해제
     */
    @Operation(summary = "리뷰 숨김 해제", description = "MANAGER·MASTER 또는 해당 가게를 소유한 OWNER가 숨김된 리뷰를 다시 노출합니다.")
    @PatchMapping("/reviews/{reviewId}/show")
    public ResponseEntity<ApiResponse<ReviewResponse>> showReview(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "리뷰 ID") @PathVariable UUID reviewId
    ) {
        ReviewResponse response =
                reviewService.showReview(
                        userDetails.getUserId(),
                        userDetails.getRole(),
                        reviewId
                );

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}