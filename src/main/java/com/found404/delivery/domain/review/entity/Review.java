package com.found404.delivery.domain.review.entity;

import com.found404.delivery.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "p_review",
        indexes = {
                @Index(
                        name = "idx_p_review_store_id",
                        columnList = "store_id"
                ),
                @Index(
                        name = "idx_p_review_user_id",
                        columnList = "user_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_p_review_order_id",
                        columnNames = "order_id"
                )
        }
)
public class Review extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(
            name = "review_id",
            nullable = false,
            updatable = false
    )
    private UUID reviewId;

    // 주문 1건당 리뷰 1개
    @Column(
            name = "order_id",
            nullable = false
    )
    private UUID orderId;

    // 리뷰 작성자
    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;

    // 리뷰 대상 가게
    @Column(
            name = "store_id",
            nullable = false
    )
    private UUID storeId;

    // 별점 1~5
    @Column(
            name = "rating",
            nullable = false
    )
    private Integer rating;

    // 리뷰 내용
    @Column(
            name = "content",
            nullable = false,
            length = 1000
    )
    private String content;

    // 관리자 또는 가게 측에서 숨김 처리
    @Column(
            name = "is_hidden",
            nullable = false
    )
    private Boolean isHidden = false;

    public static Review create(
            UUID orderId,
            Long userId,
            UUID storeId,
            Integer rating,
            String content
    ) {
        Review review = new Review();

        review.orderId = orderId;
        review.userId = userId;
        review.storeId = storeId;
        review.rating = rating;
        review.content = content;
        review.isHidden = false;

        return review;
    }

    public void update(
            Integer rating,
            String content
    ) {
        this.rating = rating;
        this.content = content;
    }

    public void hide() {
        this.isHidden = true;
    }

    public void show() {
        this.isHidden = false;
    }
}