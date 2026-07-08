package com.found404.delivery.menu.domain.repository;

import com.found404.delivery.menu.domain.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID> {
}