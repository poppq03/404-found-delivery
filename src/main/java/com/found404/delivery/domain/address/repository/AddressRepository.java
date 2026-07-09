package com.found404.delivery.domain.address.repository;

import com.found404.delivery.domain.address.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    Page<Address> findAllByUserId(Long userId, Pageable pageable);

    List<Address> findAllByUserIdAndIsDefaultTrue(Long userId);
}