package com.found404.delivery.domain.orderItem.repository;

import com.found404.delivery.domain.orderItem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
