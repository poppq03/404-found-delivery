package com.found404.delivery.domain.airequest.repository;

import com.found404.delivery.domain.airequest.entity.AiRequest;
import com.found404.delivery.domain.airequest.entity.AiRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AiRequestRepository extends JpaRepository<AiRequest, UUID> {

    @Query("""
            SELECT a FROM AiRequest a
            WHERE (:userId IS NULL OR a.userId = :userId)
              AND (:menuId IS NULL OR a.menuId = :menuId)
              AND (:status IS NULL OR a.status = :status)
            """)
    Page<AiRequest> search(@Param("userId") Long userId,
                           @Param("menuId") UUID menuId,
                           @Param("status") AiRequestStatus status,
                           Pageable pageable);
}