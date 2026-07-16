package com.found404.delivery.domain.order.repository;

import com.found404.delivery.domain.order.dto.OrderListResponseDto;
import com.found404.delivery.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findAllByUserId(Long userId, Pageable pageable);

    Page<Order> findAllByStoreId(UUID storeId, Pageable pageable);
}
