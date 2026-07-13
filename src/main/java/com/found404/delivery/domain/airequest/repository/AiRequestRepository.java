package com.found404.delivery.domain.airequest.repository;

import com.found404.delivery.domain.airequest.entity.AiRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AiRequestRepository extends JpaRepository<AiRequest, UUID> {
}
