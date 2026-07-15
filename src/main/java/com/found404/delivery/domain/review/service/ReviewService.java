package com.found404.delivery.domain.review.service;

import com.found404.delivery.domain.review.dto.ReviewCreateRequest;
import com.found404.delivery.domain.review.dto.ReviewResponse;
import com.found404.delivery.domain.review.dto.ReviewUpdateRequest;
import com.found404.delivery.domain.review.entity.Review;
import com.found404.delivery.domain.review.repository.ReviewRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * 리뷰 생성
     */
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        validateRating(request.rating());
        validateAlreadyReviewedOrder(request.orderId());

        // TODO: 로그인/인증 기능 연결 후 JWT에서 로그인 사용자 ID 가져오기
        Long userId = 1L;

        // TODO: Order 도메인 연결 후 아래 내용 검증
        // 1. 실제 존재하는 주문인지
        // 2. 배송 완료된 주문인지
        // 3. 요청한 storeId와 주문의 storeId가 같은지
        // 4. 로그인 사용자가 해당 주문의 소유자인지

        Review review = Review.create(
                request.orderId(),
                userId,
                request.storeId(),
                request.rating(),
                request.content()
        );

        Review savedReview = reviewRepository.save(review);

        return ReviewResponse.from(savedReview);
    }

    /**
     * 리뷰 단건 조회
     */
    public ReviewResponse getReview(UUID reviewId) {
        Review review = findReview(reviewId);

        return ReviewResponse.from(review);
    }

    /**
     * 가게별 공개 리뷰 목록 조회
     */
    public List<ReviewResponse> getReviewsByStoreId(UUID storeId) {
        return reviewRepository
                .findAllByStoreIdAndIsHiddenFalse(storeId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public ReviewResponse updateReview(
            UUID reviewId,
            ReviewUpdateRequest request
    ) {
        validateRating(request.rating());

        Review review = findReview(reviewId);

        // TODO: 로그인/인증 연결 후 리뷰 작성자 본인인지 검증
        review.update(
                request.rating(),
                request.content()
        );

        return ReviewResponse.from(review);
    }

    /**
     * 리뷰 숨김 처리
     */
    @Transactional
    public ReviewResponse hideReview(UUID reviewId) {
        Review review = findReview(reviewId);

        // TODO: MANAGER 또는 해당 가게 OWNER 권한인지 검증
        review.hide();

        return ReviewResponse.from(review);
    }

    /**
     * 리뷰 숨김 해제
     */
    @Transactional
    public ReviewResponse showReview(UUID reviewId) {
        Review review = findReview(reviewId);

        // TODO: MANAGER 또는 해당 가게 OWNER 권한인지 검증
        review.show();

        return ReviewResponse.from(review);
    }

    /**
     * 리뷰 조회 공통 메서드
     */
    private Review findReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.REVIEW_NOT_FOUND)
                );
    }

    /**
     * 주문별 중복 리뷰 검증
     */
    private void validateAlreadyReviewedOrder(UUID orderId) {
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new CustomException(
                    ErrorCode.ALREADY_REVIEWED_ORDER
            );
        }
    }

    /**
     * 별점 범위 검증
     */
    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new CustomException(
                    ErrorCode.INVALID_RATING
            );
        }
    }
}