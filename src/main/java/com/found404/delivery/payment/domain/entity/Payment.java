package com.found404.delivery.payment.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity //DB 테이블과 연결되는 클래스
@Table(
        name = "p_payment",
        indexes = {
                @Index(name = "idx_p_payment_user_id", columnList = "user_id")
                // 사용자 기준 결제 빠르게 조회 가능
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_p_payment_order_id", columnNames = "order_id")
                // 주문 1건당 결제 1건
        }
)
public class Payment {
    @Id
    @UuidGenerator
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method",nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status",nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    protected Payment() {
    }

    @PrePersist  // DB에 처음 저장되기 직전에 실행
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    @PreUpdate   // DB에서 수정되기 직전에 실행
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
