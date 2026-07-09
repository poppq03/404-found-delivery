package com.found404.delivery.domain.address.repository;

import com.found404.delivery.domain.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
