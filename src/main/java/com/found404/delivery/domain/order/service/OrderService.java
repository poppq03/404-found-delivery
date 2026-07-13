package com.found404.delivery.domain.order.service;

import com.found404.delivery.domain.order.dto.OrderListResponseDto;
import com.found404.delivery.domain.order.dto.OrderRequestDto;
import com.found404.delivery.domain.order.dto.OrderResponseDto;
import com.found404.delivery.domain.order.entity.Order;
import com.found404.delivery.domain.order.repository.OrderRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
}
    private final OrderRepository orderRepository;

    @Transactional
    public OrderResponseDto createOrder(Long userId, @Valid OrderRequestDto request) {
        Order order = Order.create(userId, request);
        Order saveOrder = orderRepository.save(order);

        return OrderResponseDto.from(saveOrder);
    }
