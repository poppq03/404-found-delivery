package com.found404.delivery.domain.region.repository;

import com.found404.delivery.domain.region.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegionRepository extends JpaRepository<Region, UUID> {
    boolean existsByName(String name);

    Optional<Region> findByRegionIdAndIsActiveTrue(UUID regionId);

    Page<Region> findAllByIsActiveTrueAndNameContaining(String name, Pageable pageable);

    Page<Region> findAllByIsActiveTrue(Pageable pageable);
}