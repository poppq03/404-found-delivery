package com.found404.delivery.domain.store.repository;

import com.found404.delivery.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {


}
