package com.found404.delivery.domain.store.repository;

import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.entity.StoreStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {

    // 사용자 :: 전체 Store 조회
    @Query("""
                SELECT s
                FROM Store s
                WHERE s.isActive = true
                AND s.status <> 'SUSPENDED'
                ORDER BY
                    CASE
                        WHEN s.status = 'OPEN' THEN 1
                        ELSE 2
                    END
            """)
    Slice<Store> findStoreList(Pageable pageable);


    // 사용자 :: 카테고리 별 조회 목록
    @Query("""
                SELECT s
                FROM Store s
                WHERE s.category.categoryId = :categoryId
                AND s.isActive = true
                AND s.status <> :status
                ORDER BY
                    CASE
                        WHEN s.status = com.found404.delivery.domain.store.entity.StoreStatus.OPEN
                        THEN 1
                        ELSE 2
                    END
            """)
    Slice<Store> findStoreListByCategory(
            @Param("categoryId") UUID categoryId,
            @Param("status") StoreStatus status,
            Pageable pageable
    );

    // 사용자 :: 키워드 name으로 검색
    @Query("""
            SELECT s
            FROM Store s
            WHERE s.isActive = true
              AND s.status <> :status
              AND LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY
                CASE
                    WHEN s.status = com.found404.delivery.domain.store.entity.StoreStatus.OPEN THEN 1
                    WHEN s.status = com.found404.delivery.domain.store.entity.StoreStatus.BREAK_TIME THEN 2
                    WHEN s.status = com.found404.delivery.domain.store.entity.StoreStatus.CLOSED THEN 3
                    ELSE 4
                END,
                s.createdAt DESC
            """)
    Slice<Store> searchStores(
            @Param("keyword") String keyword,
            @Param("status") StoreStatus storeStatus,
            Pageable pageable
    );


    // 사용자  :: 스토어 상세 페이지 조회
    Optional<Store> findByStoreIdAndIsActiveTrueAndStatusNot(UUID storeId, StoreStatus status);

    // 관리자 조회 목록

    Page<Store> findAll(Pageable pageable);

    Slice<Store> findByStatusAndIsActiveTrue(StoreStatus storeStatus, Pageable pageable);


}
