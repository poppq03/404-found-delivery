package com.found404.delivery.orderitem.domain.repository;

import com.found404.delivery.orderitem.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
