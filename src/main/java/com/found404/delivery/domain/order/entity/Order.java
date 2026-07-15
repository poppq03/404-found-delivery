package com.found404.delivery.domain.order.entity;

import com.found404.delivery.domain.address.entity.Address;
import com.found404.delivery.domain.order.dto.OrderRequestDto;
import com.found404.delivery.global.entity.BaseEntity;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "p_order",
        indexes = {
                @Index(name = "idx_p_order_user_id", columnList = "user_id"),
                @Index(name = "idx_p_order_store_id", columnList = "store_id"),
                @Index(name = "idx_p_order_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Id
    @UuidGenerator
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "store_id", nullable = false)
    private UUID storeId;

    @Column(name = "address_id", nullable = false)
    private UUID addressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.REQUESTED;

    @Column(name = "total_menu_price", nullable = false)
    private int totalMenuPrice;

    @Column(name = "delivery_fee", nullable = false)
    private int deliveryFee = 0;

    @Column(name = "discount_price", nullable = false)
    private int discountPrice = 0;

    @Column(name = "total_price", nullable = false)
    private int totalPrice;

    @Column(name = "delivery_address", nullable = false, length = 255)
    private String deliveryAddress;

    @Column(name = "delivery_detail_address", length = 255)
    private String deliveryDetailAddress;

    @Column(name = "delivery_request", length = 255)
    private String deliveryRequest;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "status_reason", length = 255)
    private String statusReason;

    public static Order create(
            Long userId,
            OrderRequestDto request,
            Address address,
            int totalMenuPrice,
            int deliveryFee,
            int discountPrice
    ) {
        Order order = new Order();
        order.userId = userId;
        order.storeId = request.getStoreId();
        order.addressId = request.getAddressId();
        order.status = OrderStatus.REQUESTED;

        order.totalMenuPrice = totalMenuPrice;
        order.deliveryFee = deliveryFee;
        order.discountPrice = discountPrice;
        order.totalPrice = totalMenuPrice + deliveryFee - discountPrice;

        order.deliveryAddress = address.getAddress();
        order.deliveryDetailAddress = address.getDetailAddress();
        order.deliveryRequest = request.getDeliveryRequest();

        return order;
    }

    public void cancel() {
        if (this.status != OrderStatus.REQUESTED) {
            throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
        }

        LocalDateTime cancelDeadline = getCreatedAt().plusMinutes(5);
        if (LocalDateTime.now().isAfter(cancelDeadline)) {
            throw new CustomException(ErrorCode.CANCEL_TIME_EXPIRED);
        }

        this.status = OrderStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void accept() {
        if (this.status != OrderStatus.REQUESTED) {
            throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.ACCEPTED;
    }

    public void reject(String reason) {
        if (this.status != OrderStatus.REQUESTED) {
            throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = OrderStatus.REJECTED;
        this.statusReason = reason;
    }

    public void changeOwnerStatus(OrderStatus nextStatus) {
        boolean validTransition =
                (this.status == OrderStatus.ACCEPTED && nextStatus == OrderStatus.COOKING)
                || (this.status == OrderStatus.COOKING && nextStatus == OrderStatus.DELIVERING)
                || (this.status == OrderStatus.DELIVERING && nextStatus == OrderStatus.COMPLETED);

        if (!validTransition) {
            throw new CustomException(ErrorCode.INVALID_ORDER_STATUS);
        }
        this.status = nextStatus;
    }

    public void changeAdminStatus(OrderStatus nextStatus) {
        if (nextStatus == null) {
            throw new CustomException(ErrorCode.INVALID_STATUS_VALUE);
        }

        this.status = nextStatus;

        if (nextStatus == OrderStatus.CANCELED) {
            this.canceledAt = LocalDateTime.now();
        }
    }
}
