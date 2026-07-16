package com.found404.delivery.domain.category.repository;

import com.found404.delivery.domain.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByName(String name);

    Optional<Category> findByCategoryIdAndIsActiveTrue(UUID categoryId);

    Page<Category> findAllByIsActiveTrueAndNameContaining(String name, Pageable pageable);

    Page<Category> findAllByIsActiveTrue(Pageable pageable);
}