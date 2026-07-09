package com.found404.delivery.domain.payment.repository;

import com.found404.delivery.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
