package com.found404.delivery.domain.review.dto;

import com.found404.delivery.domain.review.entity.Review;

import java.util.UUID;

public record ReviewResponse(
        UUID reviewId,
        UUID orderId,
        Long userId,
        UUID storeId,
        Integer rating,
        String content,
        Boolean isHidden
) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getReviewId(),
                review.getOrderId(),
                review.getUserId(),
                review.getStoreId(),
                review.getRating(),
                review.getContent(),
                review.getIsHidden()
        );
    }
}