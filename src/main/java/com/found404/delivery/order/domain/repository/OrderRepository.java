package com.found404.delivery.order.domain.repository;

import com.found404.delivery.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
