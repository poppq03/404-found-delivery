package com.found404.delivery.domain.review.service;

import com.found404.delivery.domain.order.entity.Order;
import com.found404.delivery.domain.order.entity.OrderStatus;
import com.found404.delivery.domain.order.repository.OrderRepository;
import com.found404.delivery.domain.review.dto.ReviewCreateRequest;
import com.found404.delivery.domain.review.dto.ReviewResponse;
import com.found404.delivery.domain.review.dto.ReviewUpdateRequest;
import com.found404.delivery.domain.review.entity.Review;
import com.found404.delivery.domain.review.repository.ReviewRepository;
import com.found404.delivery.domain.store.repository.StoreRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;

    /**
     * 리뷰 생성
     */
    @Transactional
    public ReviewResponse createReview(
            Long userId,
            ReviewCreateRequest request
    ) {
        validateRating(request.rating());

        // 실제 존재하는 주문인지 확인
        Order order = findOrder(request.orderId());

        // 로그인 사용자가 해당 주문을 한 사용자인지 확인
        validateOrderOwner(order, userId);

        // 주문이 완료 상태인지 확인
        validateOrderCompleted(order);

        // 해당 주문에 이미 작성된 리뷰가 있는지 확인
        validateAlreadyReviewedOrder(order.getId());

        /*
         * storeId는 클라이언트가 전달한 값을 사용하지 않고
         * 실제 주문에 저장된 storeId를 사용한다.
         */
        Review review = Review.create(
                order.getId(),
                userId,
                order.getStoreId(),
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
            Long userId,
            UUID reviewId,
            ReviewUpdateRequest request
    ) {
        validateRating(request.rating());

        Review review = findReview(reviewId);

        // 로그인 사용자가 리뷰 작성자인지 확인
        validateReviewAuthor(review, userId);

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
    public ReviewResponse hideReview(
            Long userId,
            String role,
            UUID reviewId
    ) {
        Review review = findReview(reviewId);

        /*
         * MANAGER, MASTER 또는
         * 해당 가게를 소유한 OWNER인지 확인
         */
        validateReviewManagePermission(
                review,
                userId,
                role
        );

        review.hide();

        return ReviewResponse.from(review);
    }

    /**
     * 리뷰 숨김 해제
     */
    @Transactional
    public ReviewResponse showReview(
            Long userId,
            String role,
            UUID reviewId
    ) {
        Review review = findReview(reviewId);

        /*
         * MANAGER, MASTER 또는
         * 해당 가게를 소유한 OWNER인지 확인
         */
        validateReviewManagePermission(
                review,
                userId,
                role
        );

        review.show();

        return ReviewResponse.from(review);
    }

    /**
     * 리뷰 조회 공통 메서드
     */
    private Review findReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.REVIEW_NOT_FOUND
                        )
                );
    }

    /**
     * 주문 조회 공통 메서드
     */
    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.ORDER_NOT_FOUND
                        )
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
     * 로그인 사용자가 주문한 본인인지 검증
     */
    private void validateOrderOwner(
            Order order,
            Long userId
    ) {
        if (!Objects.equals(order.getUserId(), userId)) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN
            );
        }
    }

    /**
     * 배송 완료된 주문인지 검증
     */
    private void validateOrderCompleted(Order order) {
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new CustomException(
                    ErrorCode.INVALID_ORDER_STATUS
            );
        }
    }

    /**
     * 로그인 사용자가 리뷰 작성자인지 검증
     */
    private void validateReviewAuthor(
            Review review,
            Long userId
    ) {
        if (!Objects.equals(review.getUserId(), userId)) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN
            );
        }
    }

    /**
     * 리뷰 숨김·해제 권한 검증
     *
     * 허용 대상:
     * 1. MANAGER
     * 2. MASTER
     * 3. 해당 리뷰 가게를 소유한 OWNER
     */
    private void validateReviewManagePermission(
            Review review,
            Long userId,
            String role
    ) {
        if ("MANAGER".equals(role) || "MASTER".equals(role)) {
            return;
        }

        if ("OWNER".equals(role)) {
            boolean isStoreOwner =
                    storeRepository.existsByStoreIdAndOwnerId(
                            review.getStoreId(),
                            userId
                    );

            if (isStoreOwner) {
                return;
            }
        }

        throw new CustomException(
                ErrorCode.FORBIDDEN
        );
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