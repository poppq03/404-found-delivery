package com.found404.delivery.domain.review.repository;

import com.found404.delivery.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByOrderId(UUID orderId);

    Optional<Review> findByOrderId(UUID orderId);

    List<Review> findAllByStoreIdAndIsHiddenFalse(UUID storeId);
}