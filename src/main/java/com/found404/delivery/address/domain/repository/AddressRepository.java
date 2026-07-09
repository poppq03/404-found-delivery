package com.found404.delivery.address.domain.repository;

import com.found404.delivery.address.domain.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
