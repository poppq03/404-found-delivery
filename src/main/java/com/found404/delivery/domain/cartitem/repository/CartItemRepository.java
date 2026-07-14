package com.found404.delivery.domain.cartitem.repository;

import com.found404.delivery.domain.cartitem.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
}
