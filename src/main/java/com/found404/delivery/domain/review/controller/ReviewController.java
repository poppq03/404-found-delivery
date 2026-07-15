package com.found404.delivery.domain.review.controller;

import com.found404.delivery.domain.review.dto.ReviewCreateRequest;
import com.found404.delivery.domain.review.dto.ReviewResponse;
import com.found404.delivery.domain.review.dto.ReviewUpdateRequest;
import com.found404.delivery.domain.review.service.ReviewService;
import com.found404.delivery.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 생성
     */
    @PostMapping("/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewResponse response = reviewService.createReview(request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 리뷰 단건 조회
     */
    @GetMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReview(
            @PathVariable UUID reviewId
    ) {
        ReviewResponse response = reviewService.getReview(reviewId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 가게별 공개 리뷰 목록 조회
     */
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviewsByStoreId(
            @PathVariable UUID storeId
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
    @PatchMapping("/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable UUID reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        ReviewResponse response =
                reviewService.updateReview(reviewId, request);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 리뷰 숨김 처리
     */
    @PatchMapping("/reviews/{reviewId}/hide")
    public ResponseEntity<ApiResponse<ReviewResponse>> hideReview(
            @PathVariable UUID reviewId
    ) {
        ReviewResponse response =
                reviewService.hideReview(reviewId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }

    /**
     * 리뷰 숨김 해제
     */
    @PatchMapping("/reviews/{reviewId}/show")
    public ResponseEntity<ApiResponse<ReviewResponse>> showReview(
            @PathVariable UUID reviewId
    ) {
        ReviewResponse response =
                reviewService.showReview(reviewId);

        return ResponseEntity.ok(
                ApiResponse.success(response)
        );
    }
}