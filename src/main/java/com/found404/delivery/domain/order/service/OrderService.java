package com.found404.delivery.domain.order.service;

import com.found404.delivery.domain.address.entity.Address;
import com.found404.delivery.domain.address.repository.AddressRepository;
import com.found404.delivery.domain.menu.service.MenuInfo;
import com.found404.delivery.domain.menu.service.MenuQueryService;
import com.found404.delivery.domain.order.dto.OrderItemRequestDto;
import com.found404.delivery.domain.order.dto.OrderListResponseDto;
import com.found404.delivery.domain.order.dto.OrderRequestDto;
import com.found404.delivery.domain.order.dto.OrderResponseDto;
import com.found404.delivery.domain.order.entity.Order;
import com.found404.delivery.domain.order.repository.OrderRepository;
import com.found404.delivery.domain.orderItem.entity.OrderItem;
import com.found404.delivery.domain.orderItem.repository.OrderItemRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final MenuQueryService menuQueryService;
    private final OrderItemRepository orderItemRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public OrderResponseDto createOrder(Long userId, @Valid OrderRequestDto request) {
        Address address = getMyAddress(userId, request.getAddressId());

        List<UUID> menuIds = request.getItems().stream()
                .map(OrderItemRequestDto::getMenuId)
                .toList();


        Map<UUID, MenuInfo> menuInfoMap = menuQueryService.getMenuInfos(menuIds);
        validateMenus(request, menuInfoMap);

        int totalMenuPrice = calculateTotalMenuPrice(request, menuInfoMap);
        int deliveryFee = 0;
        int discountPrice = 0;

        Order order = Order.create(
                userId,
                request,
                address,
                totalMenuPrice,
                deliveryFee,
                discountPrice
        );

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = request.getItems().stream()
                .map(item -> {
                    MenuInfo menuInfo = menuInfoMap.get(item.getMenuId());

                    return OrderItem.create(
                            savedOrder.getId(),
                            menuInfo.menuId(),
                            menuInfo.name(),
                            menuInfo.price(),
                            item.getQuantity()
                    );
                }).toList();

        List<OrderItem> savedOrderItems = orderItemRepository.saveAll(orderItems);
        return OrderResponseDto.from(savedOrder, savedOrderItems);
    }

    public Page<OrderListResponseDto> getMyOrders(Long userId, int page, int size) {
        validatePageSize(size);
        Pageable pageable = PageRequest.of(page, size);

        return orderRepository.findAllByUserId(userId, pageable)
                .map(OrderListResponseDto::from);
    }

    public OrderResponseDto getMyOrder(Long userId, UUID orderId) {
        Order order = findOrderByIdAndUserId(orderId, userId);
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);

        return OrderResponseDto.from(order, orderItems);
    }

    @Transactional
    public OrderResponseDto cancelOrder(Long userId, UUID orderId) {
        Order order = findOrderByIdAndUserId(orderId, userId);
        order.cancel();

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);

        return OrderResponseDto.from(order, orderItems);
    }

    private void validatePageSize(int size) {
        if (size != 10 && size != 30 && size != 50) {
            throw new CustomException(ErrorCode.INVALID_PAGE_SIZE);
        }
    }


    private Address getMyAddress(Long userId, UUID addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

        if (!address.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ADDRESS);
        }

        return address;
    }

    private void validateMenus(OrderRequestDto request, Map<UUID, MenuInfo> menuInfoMap) {
        for (OrderItemRequestDto item : request.getItems()) {
            MenuInfo menuInfo = menuInfoMap.get(item.getMenuId());
            if (menuInfo == null) {
                throw new CustomException(ErrorCode.MENU_NOT_FOUND);
            }

            if (!menuInfo.storeId().equals(request.getStoreId())) {
                throw new CustomException(ErrorCode.DIFFERENT_STORE_MENU);
            }

            if (menuInfo.hidden() || menuInfo.soldOut()) {
                throw new CustomException(ErrorCode.MENU_UNAVAILABLE);
            }
        }
    }


    private int calculateTotalMenuPrice(@Valid OrderRequestDto request, Map<UUID, MenuInfo> menuInfoMap) {
        return request.getItems().stream()
                .mapToInt(item -> {
                    MenuInfo menuInfo = menuInfoMap.get(item.getMenuId());
                    return menuInfo.price() * item.getQuantity();
                }).sum();
    }

    private Order findOrderByIdAndUserId(UUID orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.NOT_ORDER_OWNER);
        }

        return order;
    }
}