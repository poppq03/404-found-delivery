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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private ReviewService reviewService;

    /**
     * 테스트용 Order Mock 객체 생성
     */
    private Order mockOrder(
            UUID orderId,
            Long userId,
            UUID storeId,
            OrderStatus status
    ) {
        Order order = org.mockito.Mockito.mock(Order.class);

        /*
         * 테스트마다 일부 getter만 사용할 수도 있어서
         * 불필요한 stubbing 오류를 막기 위해 lenient 사용
         */
        lenient().when(order.getId()).thenReturn(orderId);
        lenient().when(order.getUserId()).thenReturn(userId);
        lenient().when(order.getStoreId()).thenReturn(storeId);
        lenient().when(order.getStatus()).thenReturn(status);

        return order;
    }

    /**
     * 테스트용 Review 객체 생성
     */
    private Review reviewWithId(
            UUID reviewId,
            UUID orderId,
            Long userId,
            UUID storeId,
            Integer rating,
            String content,
            Boolean isHidden
    ) {
        Review review = Review.create(
                orderId,
                userId,
                storeId,
                rating,
                content
        );

        ReflectionTestUtils.setField(
                review,
                "reviewId",
                reviewId
        );

        ReflectionTestUtils.setField(
                review,
                "isHidden",
                isHidden
        );

        return review;
    }

    /**
     * CustomException의 ErrorCode 검증
     */
    private void assertErrorCode(
            org.assertj.core.api.ThrowableAssert.ThrowingCallable action,
            ErrorCode expectedErrorCode
    ) {
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting(
                        error ->
                                ((CustomException) error).getErrorCode()
                )
                .isEqualTo(expectedErrorCode);
    }

    // ===== createReview =====

    @Test
    @DisplayName("리뷰 생성 성공 - 완료된 본인 주문이면 리뷰를 저장한다")
    void createReview_success() {
        // given
        Long userId = 1L;
        UUID orderId = UUID.randomUUID();
        UUID orderStoreId = UUID.randomUUID();
        UUID requestStoreId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        ReviewCreateRequest request =
                new ReviewCreateRequest(
                        orderId,
                        requestStoreId,
                        5,
                        "음식이 맛있고 배달도 빨랐어요."
                );

        Order order = mockOrder(
                orderId,
                userId,
                orderStoreId,
                OrderStatus.COMPLETED
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(reviewRepository.existsByOrderId(orderId))
                .thenReturn(false);

        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation -> {
                    Review review = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            review,
                            "reviewId",
                            reviewId
                    );

                    return review;
                });

        // when
        ReviewResponse response =
                reviewService.createReview(
                        userId,
                        request
                );

        // then
        assertThat(response.reviewId())
                .isEqualTo(reviewId);

        assertThat(response.orderId())
                .isEqualTo(orderId);

        assertThat(response.userId())
                .isEqualTo(userId);

        /*
         * 요청으로 받은 requestStoreId가 아니라
         * 실제 주문의 orderStoreId가 저장되는지 확인
         */
        assertThat(response.storeId())
                .isEqualTo(orderStoreId);

        assertThat(response.storeId())
                .isNotEqualTo(requestStoreId);

        assertThat(response.rating())
                .isEqualTo(5);

        assertThat(response.content())
                .isEqualTo("음식이 맛있고 배달도 빨랐어요.");

        assertThat(response.isHidden())
                .isFalse();

        verify(reviewRepository)
                .save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 별점이 null이면 INVALID_RATING 예외")
    void createReview_fail_nullRating() {
        // given
        ReviewCreateRequest request =
                new ReviewCreateRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        null,
                        "리뷰 내용"
                );

        // when & then
        assertErrorCode(
                () -> reviewService.createReview(
                        1L,
                        request
                ),
                ErrorCode.INVALID_RATING
        );

        verify(orderRepository, never())
                .findById(any());

        verify(reviewRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 별점이 1점 미만이면 INVALID_RATING 예외")
    void createReview_fail_ratingBelowMinimum() {
        // given
        ReviewCreateRequest request =
                new ReviewCreateRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        0,
                        "리뷰 내용"
                );

        // when & then
        assertErrorCode(
                () -> reviewService.createReview(
                        1L,
                        request
                ),
                ErrorCode.INVALID_RATING
        );

        verify(orderRepository, never())
                .findById(any());
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 별점이 5점 초과면 INVALID_RATING 예외")
    void createReview_fail_ratingAboveMaximum() {
        // given
        ReviewCreateRequest request =
                new ReviewCreateRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        6,
                        "리뷰 내용"
                );

        // when & then
        assertErrorCode(
                () -> reviewService.createReview(
                        1L,
                        request
                ),
                ErrorCode.INVALID_RATING
        );

        verify(orderRepository, never())
                .findById(any());
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 주문이 존재하지 않으면 ORDER_NOT_FOUND 예외")
    void createReview_fail_orderNotFound() {
        // given
        UUID orderId = UUID.randomUUID();

        ReviewCreateRequest request =
                new ReviewCreateRequest(
                        orderId,
                        UUID.randomUUID(),
                        5,
                        "리뷰 내용"
                );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // when & then
        assertErrorCode(
                () -> reviewService.createReview(
                        1L,
                        request
                ),
                ErrorCode.ORDER_NOT_FOUND
        );

        verify(reviewRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 본인의 주문이 아니면 FORBIDDEN 예외")
    void createReview_fail_notOrderOwner() {
        // given
        Long loginUserId = 1L;
        Long orderOwnerId = 2L;
        UUID orderId = UUID.randomUUID();

        ReviewCreateRequest request =
                new ReviewCreateRequest(
                        orderId,
                        UUID.randomUUID(),
                        5,
                        "리뷰 내용"
                );

        Order order = mockOrder(
                orderId,
                orderOwnerId,
                UUID.randomUUID(),
                OrderStatus.COMPLETED
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // when & then
        assertErrorCode(
                () -> reviewService.createReview(
                        loginUserId,
                        request
                ),
                ErrorCode.FORBIDDEN
        );

        verify(reviewRepository, never())
                .existsByOrderId(any());

        verify(reviewRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 완료되지 않은 주문이면 INVALID_ORDER_STATUS 예외")
    void createReview_fail_orderNotCompleted() {
        // given
        Long userId = 1L;
        UUID orderId = UUID.randomUUID();

        ReviewCreateRequest request =
                new ReviewCreateRequest(
                        orderId,
                        UUID.randomUUID(),
                        5,
                        "리뷰 내용"
                );

        Order order = mockOrder(
                orderId,
                userId,
                UUID.randomUUID(),
                OrderStatus.DELIVERING
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        // when & then
        assertErrorCode(
                () -> reviewService.createReview(
                        userId,
                        request
                ),
                ErrorCode.INVALID_ORDER_STATUS
        );

        verify(reviewRepository, never())
                .existsByOrderId(any());

        verify(reviewRepository, never())
                .save(any());
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 이미 리뷰가 작성된 주문이면 ALREADY_REVIEWED_ORDER 예외")
    void createReview_fail_alreadyReviewedOrder() {
        // given
        Long userId = 1L;
        UUID orderId = UUID.randomUUID();

        ReviewCreateRequest request =
                new ReviewCreateRequest(
                        orderId,
                        UUID.randomUUID(),
                        5,
                        "리뷰 내용"
                );

        Order order = mockOrder(
                orderId,
                userId,
                UUID.randomUUID(),
                OrderStatus.COMPLETED
        );

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        when(reviewRepository.existsByOrderId(orderId))
                .thenReturn(true);

        // when & then
        assertErrorCode(
                () -> reviewService.createReview(
                        userId,
                        request
                ),
                ErrorCode.ALREADY_REVIEWED_ORDER
        );

        verify(reviewRepository, never())
                .save(any());
    }

    // ===== getReview =====

    @Test
    @DisplayName("리뷰 단건 조회 성공")
    void getReview_success() {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                orderId,
                1L,
                storeId,
                5,
                "맛있어요.",
                false
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        // when
        ReviewResponse response =
                reviewService.getReview(reviewId);

        // then
        assertThat(response.reviewId())
                .isEqualTo(reviewId);

        assertThat(response.orderId())
                .isEqualTo(orderId);

        assertThat(response.storeId())
                .isEqualTo(storeId);

        assertThat(response.rating())
                .isEqualTo(5);

        assertThat(response.content())
                .isEqualTo("맛있어요.");

        assertThat(response.isHidden())
                .isFalse();
    }

    @Test
    @DisplayName("리뷰 단건 조회 실패 - 리뷰가 없으면 REVIEW_NOT_FOUND 예외")
    void getReview_fail_notFound() {
        // given
        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        // when & then
        assertErrorCode(
                () -> reviewService.getReview(reviewId),
                ErrorCode.REVIEW_NOT_FOUND
        );
    }

    // ===== getReviewsByStoreId =====

    @Test
    @DisplayName("가게별 공개 리뷰 목록 조회 성공 - 숨김되지 않은 리뷰를 반환한다")
    void getReviewsByStoreId_success() {
        // given
        UUID storeId = UUID.randomUUID();

        Review firstReview = reviewWithId(
                UUID.randomUUID(),
                UUID.randomUUID(),
                1L,
                storeId,
                5,
                "정말 맛있어요.",
                false
        );

        Review secondReview = reviewWithId(
                UUID.randomUUID(),
                UUID.randomUUID(),
                2L,
                storeId,
                4,
                "배달이 빨랐어요.",
                false
        );

        when(
                reviewRepository
                        .findAllByStoreIdAndIsHiddenFalse(storeId)
        ).thenReturn(
                List.of(
                        firstReview,
                        secondReview
                )
        );

        // when
        List<ReviewResponse> responses =
                reviewService.getReviewsByStoreId(storeId);

        // then
        assertThat(responses)
                .hasSize(2);

        assertThat(responses)
                .extracting(ReviewResponse::rating)
                .containsExactly(5, 4);

        assertThat(responses)
                .allMatch(response -> !response.isHidden());
    }

    @Test
    @DisplayName("가게별 공개 리뷰 목록 조회 성공 - 리뷰가 없으면 빈 목록을 반환한다")
    void getReviewsByStoreId_success_emptyList() {
        // given
        UUID storeId = UUID.randomUUID();

        when(
                reviewRepository
                        .findAllByStoreIdAndIsHiddenFalse(storeId)
        ).thenReturn(List.of());

        // when
        List<ReviewResponse> responses =
                reviewService.getReviewsByStoreId(storeId);

        // then
        assertThat(responses)
                .isEmpty();
    }

    // ===== updateReview =====

    @Test
    @DisplayName("리뷰 수정 성공 - 작성자가 별점과 내용을 수정한다")
    void updateReview_success() {
        // given
        Long userId = 1L;
        UUID reviewId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                UUID.randomUUID(),
                userId,
                UUID.randomUUID(),
                3,
                "기존 리뷰",
                false
        );

        ReviewUpdateRequest request =
                new ReviewUpdateRequest(
                        5,
                        "수정된 리뷰입니다."
                );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        // when
        ReviewResponse response =
                reviewService.updateReview(
                        userId,
                        reviewId,
                        request
                );

        // then
        assertThat(response.rating())
                .isEqualTo(5);

        assertThat(response.content())
                .isEqualTo("수정된 리뷰입니다.");

        assertThat(review.getRating())
                .isEqualTo(5);

        assertThat(review.getContent())
                .isEqualTo("수정된 리뷰입니다.");
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 별점이 유효하지 않으면 INVALID_RATING 예외")
    void updateReview_fail_invalidRating() {
        // given
        ReviewUpdateRequest request =
                new ReviewUpdateRequest(
                        6,
                        "수정된 리뷰"
                );

        // when & then
        assertErrorCode(
                () -> reviewService.updateReview(
                        1L,
                        UUID.randomUUID(),
                        request
                ),
                ErrorCode.INVALID_RATING
        );

        verify(reviewRepository, never())
                .findById(any());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 리뷰가 없으면 REVIEW_NOT_FOUND 예외")
    void updateReview_fail_notFound() {
        // given
        UUID reviewId = UUID.randomUUID();

        ReviewUpdateRequest request =
                new ReviewUpdateRequest(
                        5,
                        "수정된 리뷰"
                );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.empty());

        // when & then
        assertErrorCode(
                () -> reviewService.updateReview(
                        1L,
                        reviewId,
                        request
                ),
                ErrorCode.REVIEW_NOT_FOUND
        );
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 작성자가 아니면 FORBIDDEN 예외")
    void updateReview_fail_notAuthor() {
        // given
        Long loginUserId = 1L;
        Long reviewAuthorId = 2L;
        UUID reviewId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                UUID.randomUUID(),
                reviewAuthorId,
                UUID.randomUUID(),
                3,
                "기존 리뷰",
                false
        );

        ReviewUpdateRequest request =
                new ReviewUpdateRequest(
                        5,
                        "몰래 수정한 리뷰"
                );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        // when & then
        assertErrorCode(
                () -> reviewService.updateReview(
                        loginUserId,
                        reviewId,
                        request
                ),
                ErrorCode.FORBIDDEN
        );

        assertThat(review.getRating())
                .isEqualTo(3);

        assertThat(review.getContent())
                .isEqualTo("기존 리뷰");
    }

    // ===== hideReview =====

    @Test
    @DisplayName("리뷰 숨김 성공 - MANAGER는 리뷰를 숨길 수 있다")
    void hideReview_success_manager() {
        // given
        UUID reviewId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                UUID.randomUUID(),
                1L,
                UUID.randomUUID(),
                5,
                "리뷰 내용",
                false
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        // when
        ReviewResponse response =
                reviewService.hideReview(
                        10L,
                        "MANAGER",
                        reviewId
                );

        // then
        assertThat(response.isHidden())
                .isTrue();

        assertThat(review.getIsHidden())
                .isTrue();

        verify(storeRepository, never())
                .existsByStoreIdAndOwnerId(
                        any(),
                        any()
                );
    }

    @Test
    @DisplayName("리뷰 숨김 성공 - MASTER는 리뷰를 숨길 수 있다")
    void hideReview_success_master() {
        // given
        UUID reviewId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                UUID.randomUUID(),
                1L,
                UUID.randomUUID(),
                5,
                "리뷰 내용",
                false
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        // when
        ReviewResponse response =
                reviewService.hideReview(
                        10L,
                        "MASTER",
                        reviewId
                );

        // then
        assertThat(response.isHidden())
                .isTrue();

        verify(storeRepository, never())
                .existsByStoreIdAndOwnerId(
                        any(),
                        any()
                );
    }

    @Test
    @DisplayName("리뷰 숨김 성공 - 해당 가게 OWNER는 리뷰를 숨길 수 있다")
    void hideReview_success_storeOwner() {
        // given
        Long ownerId = 10L;
        UUID reviewId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                UUID.randomUUID(),
                1L,
                storeId,
                5,
                "리뷰 내용",
                false
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(
                storeRepository.existsByStoreIdAndOwnerId(
                        storeId,
                        ownerId
                )
        ).thenReturn(true);

        // when
        ReviewResponse response =
                reviewService.hideReview(
                        ownerId,
                        "OWNER",
                        reviewId
                );

        // then
        assertThat(response.isHidden())
                .isTrue();

        verify(storeRepository)
                .existsByStoreIdAndOwnerId(
                        storeId,
                        ownerId
                );
    }

    @Test
    @DisplayName("리뷰 숨김 실패 - 해당 가게 소유자가 아닌 OWNER면 FORBIDDEN 예외")
    void hideReview_fail_notStoreOwner() {
        // given
        Long ownerId = 10L;
        UUID reviewId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                UUID.randomUUID(),
                1L,
                storeId,
                5,
                "리뷰 내용",
                false
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(
                storeRepository.existsByStoreIdAndOwnerId(
                        storeId,
                        ownerId
                )
        ).thenReturn(false);

        // when & then
        assertErrorCode(
                () -> reviewService.hideReview(
                        ownerId,
                        "OWNER",
                        reviewId
                ),
                ErrorCode.FORBIDDEN
        );

        assertThat(review.getIsHidden())
                .isFalse();
    }

    @Test
    @DisplayName("리뷰 숨김 실패 - CUSTOMER 권한이면 FORBIDDEN 예외")
    void hideReview_fail_customer() {
        // given
        UUID reviewId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                UUID.randomUUID(),
                1L,
                UUID.randomUUID(),
                5,
                "리뷰 내용",
                false
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        // when & then
        assertErrorCode(
                () -> reviewService.hideReview(
                        1L,
                        "CUSTOMER",
                        reviewId
                ),
                ErrorCode.FORBIDDEN
        );

        assertThat(review.getIsHidden())
                .isFalse();

        verify(storeRepository, never())
                .existsByStoreIdAndOwnerId(
                        any(),
                        any()
                );
    }

    // ===== showReview =====

    @Test
    @DisplayName("리뷰 숨김 해제 성공 - MANAGER는 숨김을 해제할 수 있다")
    void showReview_success_manager() {
        // given
        UUID reviewId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                UUID.randomUUID(),
                1L,
                UUID.randomUUID(),
                5,
                "리뷰 내용",
                true
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        // when
        ReviewResponse response =
                reviewService.showReview(
                        10L,
                        "MANAGER",
                        reviewId
                );

        // then
        assertThat(response.isHidden())
                .isFalse();

        assertThat(review.getIsHidden())
                .isFalse();
    }

    @Test
    @DisplayName("리뷰 숨김 해제 성공 - 해당 가게 OWNER는 숨김을 해제할 수 있다")
    void showReview_success_storeOwner() {
        // given
        Long ownerId = 10L;
        UUID reviewId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                UUID.randomUUID(),
                1L,
                storeId,
                5,
                "리뷰 내용",
                true
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        when(
                storeRepository.existsByStoreIdAndOwnerId(
                        storeId,
                        ownerId
                )
        ).thenReturn(true);

        // when
        ReviewResponse response =
                reviewService.showReview(
                        ownerId,
                        "OWNER",
                        reviewId
                );

        // then
        assertThat(response.isHidden())
                .isFalse();

        assertThat(review.getIsHidden())
                .isFalse();
    }

    @Test
    @DisplayName("리뷰 숨김 해제 실패 - 권한이 없으면 FORBIDDEN 예외")
    void showReview_fail_forbidden() {
        // given
        UUID reviewId = UUID.randomUUID();

        Review review = reviewWithId(
                reviewId,
                UUID.randomUUID(),
                1L,
                UUID.randomUUID(),
                5,
                "리뷰 내용",
                true
        );

        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(review));

        // when & then
        assertErrorCode(
                () -> reviewService.showReview(
                        1L,
                        "CUSTOMER",
                        reviewId
                ),
                ErrorCode.FORBIDDEN
        );

        assertThat(review.getIsHidden())
                .isTrue();
    }
}