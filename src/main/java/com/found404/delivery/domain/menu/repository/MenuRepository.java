package com.found404.delivery.domain.menu.repository;

import com.found404.delivery.domain.menu.entity.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {

    // 검색 조건이 복잡해지면 QueryDSL 고려
    @Query("""
            SELECT m FROM Menu m
            WHERE m.storeId = :storeId
            AND m.name LIKE :keyword
            AND (:soldOut IS NULL OR m.isSoldOut = :soldOut)
            AND (:includeHidden = true OR m.isHidden = false)
            """)
    Page<Menu> search(@Param("storeId") UUID storeId,
                      @Param("keyword") String keyword,
                      @Param("soldOut") Boolean soldOut,
                      @Param("includeHidden") boolean includeHidden,
                      Pageable pageable);
}