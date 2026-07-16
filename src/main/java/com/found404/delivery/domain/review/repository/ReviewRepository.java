package com.found404.delivery.domain.review.repository;

import com.found404.delivery.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByOrderId(UUID orderId);

    Optional<Review> findByOrderId(UUID orderId);

    List<Review> findAllByStoreIdAndIsHiddenFalse(UUID storeId);

    @Query("""
            SELECT
                r.storeId AS storeId,
                AVG(r.rating) AS averageRating
            FROM Review r
            WHERE r.storeId IN :storeIds
              AND r.isHidden = false
              AND r.deletedAt IS NULL
            GROUP BY r.storeId
            """)
    List<StoreRatingAverage> findAverageRatingsByStoreIds(
            @Param("storeIds") Collection<UUID> storeIds
    );
}