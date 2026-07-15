package com.found404.delivery.domain.order;

import com.found404.delivery.domain.address.dto.AddressRequestDto;
import com.found404.delivery.domain.address.entity.Address;
import com.found404.delivery.domain.address.repository.AddressRepository;
import com.found404.delivery.domain.menu.service.MenuInfo;
import com.found404.delivery.domain.menu.service.MenuQueryService;
import com.found404.delivery.domain.order.dto.OrderItemRequestDto;
import com.found404.delivery.domain.order.dto.OrderRejectRequestDto;
import com.found404.delivery.domain.order.dto.OrderRequestDto;
import com.found404.delivery.domain.order.dto.OrderResponseDto;
import com.found404.delivery.domain.order.dto.OrderStatusUpdateRequestDto;
import com.found404.delivery.domain.order.entity.Order;
import com.found404.delivery.domain.order.entity.OrderStatus;
import com.found404.delivery.domain.order.repository.OrderRepository;
import com.found404.delivery.domain.order.service.OrderService;
import com.found404.delivery.domain.orderitem.entity.OrderItem;
import com.found404.delivery.domain.orderitem.repository.OrderItemRepository;
import com.found404.delivery.domain.payment.service.PaymentService;
import com.found404.delivery.domain.store.entity.Store;
import com.found404.delivery.domain.store.entity.StoreStatus;
import com.found404.delivery.domain.store.repository.StoreRepository;
import com.found404.delivery.global.exception.CustomException;
import com.found404.delivery.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MenuQueryService menuQueryService;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문을 생성하면 배송지와 메뉴를 검증하고 주문상품까지 저장한다")
    void createOrder() {
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderRequestDto request = orderRequest(storeId, addressId, "문 앞에 놓아주세요", List.of(orderItemRequest(menuId, 2)));
        Address address = address(userId, addressId);
        Store store = store(storeId, 10_000, 3_000, StoreStatus.OPEN, true);
        MenuInfo menuInfo = new MenuInfo(menuId, storeId, "테스트 메뉴", 12_000, null, false, false);

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(menuQueryService.getMenuInfos(List.of(menuId))).willReturn(Map.of(menuId, menuInfo));
        given(orderRepository.save(any(Order.class)))
                .willAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    ReflectionTestUtils.setField(order, "id", orderId);
                    ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
                    return order;
                });
        given(orderItemRepository.saveAll(any()))
                .willAnswer(invocation -> {
                    List<OrderItem> orderItems = invocation.getArgument(0);
                    ReflectionTestUtils.setField(orderItems.get(0), "id", UUID.randomUUID());
                    return orderItems;
                });

        OrderResponseDto response = orderService.createOrder(userId, request);

        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getStoreId()).isEqualTo(storeId);
        assertThat(response.getAddressId()).isEqualTo(addressId);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.REQUESTED);
        assertThat(response.getTotalMenuPrice()).isEqualTo(24_000);
        assertThat(response.getDeliveryFee()).isEqualTo(3_000);
        assertThat(response.getTotalPrice()).isEqualTo(27_000);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getMenuName()).isEqualTo("테스트 메뉴");
        assertThat(response.getItems().get(0).getTotalPrice()).isEqualTo(24_000);
        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(any());
    }

    @Test
    @DisplayName("주문 생성 시 배송지가 본인 소유가 아니면 예외가 발생한다")
    void createOrderForbiddenAddress() {
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        OrderRequestDto request = orderRequest(storeId, addressId, "문 앞", List.of(orderItemRequest(menuId, 1)));
        Address address = address(2L, addressId);

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address));

        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_ADDRESS);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 시 존재하지 않는 메뉴가 있으면 예외가 발생한다")
    void createOrderMenuNotFound() {
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        OrderRequestDto request = orderRequest(storeId, addressId, "문 앞", List.of(orderItemRequest(menuId, 1)));

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address(userId, addressId)));
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store(storeId, 1_000, 0, StoreStatus.OPEN, true)));
        given(menuQueryService.getMenuInfos(List.of(menuId))).willReturn(Map.of());

        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MENU_NOT_FOUND);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("주문 생성 시 다른 가게 메뉴가 포함되면 예외가 발생한다")
    void createOrderDifferentStoreMenu() {
        Long userId = 1L;
        UUID requestStoreId = UUID.randomUUID();
        UUID menuStoreId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        OrderRequestDto request = orderRequest(requestStoreId, addressId, "문 앞", List.of(orderItemRequest(menuId, 1)));
        MenuInfo menuInfo = new MenuInfo(menuId, menuStoreId, "다른 가게 메뉴", 10_000, null, false, false);

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address(userId, addressId)));
        given(storeRepository.findById(requestStoreId)).willReturn(Optional.of(store(requestStoreId, 1_000, 0, StoreStatus.OPEN, true)));
        given(menuQueryService.getMenuInfos(List.of(menuId))).willReturn(Map.of(menuId, menuInfo));

        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DIFFERENT_STORE_MENU);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("숨김 또는 품절 메뉴는 주문할 수 없다")
    void createOrderUnavailableMenu() {
        Long userId = 1L;
        UUID storeId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID menuId = UUID.randomUUID();
        OrderRequestDto request = orderRequest(storeId, addressId, "문 앞", List.of(orderItemRequest(menuId, 1)));
        MenuInfo menuInfo = new MenuInfo(menuId, storeId, "품절 메뉴", 10_000, null, false, true);

        given(addressRepository.findById(addressId)).willReturn(Optional.of(address(userId, addressId)));
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store(storeId, 1_000, 0, StoreStatus.OPEN, true)));
        given(menuQueryService.getMenuInfos(List.of(menuId))).willReturn(Map.of(menuId, menuInfo));

        assertThatThrownBy(() -> orderService.createOrder(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MENU_UNAVAILABLE);
    }

    @Test
    @DisplayName("본인 주문을 취소한다")
    void cancelOrder() {
        Long userId = 1L;
        UUID orderId = UUID.randomUUID();
        Order order = order(userId, orderId, UUID.randomUUID(), UUID.randomUUID());
        OrderItem orderItem = orderItem(orderId, UUID.randomUUID(), "테스트 메뉴", 10_000, 1);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(orderItemRepository.findAllByOrderId(orderId)).willReturn(List.of(orderItem));

        OrderResponseDto response = orderService.cancelOrder(userId, orderId);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(response.getCanceledAt()).isNotNull();
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("본인 주문이 아니면 취소할 수 없다")
    void cancelOrderNotOwner() {
        UUID orderId = UUID.randomUUID();
        Order order = order(2L, orderId, UUID.randomUUID(), UUID.randomUUID());

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, orderId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_ORDER_OWNER);
    }

    @Test
    @DisplayName("사장님은 본인 가게 주문 목록을 조회한다")
    void getMyStoreOrders() {
        Long ownerId = 1L;
        UUID storeId = UUID.randomUUID();
        Order order = order(10L, UUID.randomUUID(), storeId, UUID.randomUUID());

        given(storeRepository.existsByStoreIdAndOwnerId(storeId, ownerId)).willReturn(true);
        given(orderRepository.findAllByStoreId(eq(storeId), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(order)));

        Page<?> response = orderService.getMyStoreOrders(ownerId, "OWNER", storeId, 0, 10);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("사장님이 아닌 사용자는 사장님 주문 API를 사용할 수 없다")
    void getMyStoreOrdersForbiddenRole() {
        assertThatThrownBy(() -> orderService.getMyStoreOrders(1L, "CUSTOMER", UUID.randomUUID(), 0, 10))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("사장님은 REQUESTED 주문을 수락할 수 있다")
    void acceptOrder() {
        Long ownerId = 1L;
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Order order = order(10L, orderId, storeId, UUID.randomUUID());

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(storeRepository.existsByStoreIdAndOwnerId(storeId, ownerId)).willReturn(true);
        given(orderItemRepository.findAllByOrderId(orderId)).willReturn(List.of());

        OrderResponseDto response = orderService.acceptOrder(ownerId, "OWNER", orderId);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    @DisplayName("관리자 주문 API는 MANAGER 또는 MASTER만 사용할 수 있다")
    void getAllOrdersForbiddenRole() {
        assertThatThrownBy(() -> orderService.getAllOrders("OWNER", 0, 10))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("관리자는 주문 상태를 변경할 수 있다")
    void changeAdminOrderStatus() {
        UUID orderId = UUID.randomUUID();
        Order order = order(1L, orderId, UUID.randomUUID(), UUID.randomUUID());
        OrderStatusUpdateRequestDto request = orderStatusUpdateRequest(OrderStatus.COMPLETED);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(orderItemRepository.findAllByOrderId(orderId)).willReturn(List.of());

        OrderResponseDto response = orderService.changeAdminOrderStatus("MANAGER", orderId, request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("주문 거절 시 거절 사유가 저장된다")
    void rejectOrder() {
        Long ownerId = 1L;
        UUID storeId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Order order = order(10L, orderId, storeId, UUID.randomUUID());
        OrderRejectRequestDto request = rejectRequest("재료 소진");

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(storeRepository.existsByStoreIdAndOwnerId(storeId, ownerId)).willReturn(true);
        given(orderItemRepository.findAllByOrderId(orderId)).willReturn(List.of());

        OrderResponseDto response = orderService.rejectOrder(ownerId, "OWNER", orderId, request);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.REJECTED);
        assertThat(response.getStatusReason()).isEqualTo("재료 소진");
    }

    private OrderRequestDto orderRequest(UUID storeId, UUID addressId, String deliveryRequest, List<OrderItemRequestDto> items) {
        OrderRequestDto request = new OrderRequestDto();
        ReflectionTestUtils.setField(request, "storeId", storeId);
        ReflectionTestUtils.setField(request, "addressId", addressId);
        ReflectionTestUtils.setField(request, "deliveryRequest", deliveryRequest);
        ReflectionTestUtils.setField(request, "items", items);
        return request;
    }

    private OrderItemRequestDto orderItemRequest(UUID menuId, int quantity) {
        OrderItemRequestDto request = new OrderItemRequestDto();
        ReflectionTestUtils.setField(request, "menuId", menuId);
        ReflectionTestUtils.setField(request, "quantity", quantity);
        return request;
    }

    private Address address(Long userId, UUID addressId) {
        AddressRequestDto request = new AddressRequestDto();
        ReflectionTestUtils.setField(request, "addressName", "집");
        ReflectionTestUtils.setField(request, "address", "서울시 강남구");
        ReflectionTestUtils.setField(request, "detailAddress", "101동");
        ReflectionTestUtils.setField(request, "receiverName", "홍길동");
        ReflectionTestUtils.setField(request, "phone", "010-1234-5678");

        Address address = Address.create(userId, request);
        ReflectionTestUtils.setField(address, "id", addressId);
        return address;
    }

    private Order order(Long userId, UUID orderId, UUID storeId, UUID addressId) {
        Address address = address(userId, addressId);
        OrderRequestDto request = orderRequest(storeId, addressId, "문 앞", List.of(orderItemRequest(UUID.randomUUID(), 1)));
        Order order = Order.create(userId, request, address, 10_000, 0, 0);
        ReflectionTestUtils.setField(order, "id", orderId);
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.now());
        return order;
    }

    private OrderItem orderItem(UUID orderId, UUID menuId, String menuName, int menuPrice, int quantity) {
        OrderItem orderItem = OrderItem.create(orderId, menuId, menuName, menuPrice, quantity);
        ReflectionTestUtils.setField(orderItem, "id", UUID.randomUUID());
        return orderItem;
    }

    private Store store(UUID storeId, int minOrderPrice, int deliveryFee, StoreStatus status, boolean isActive) {
        return Store.builder()
                .storeId(storeId)
                .name("테스트 가게")
                .phoneNumber("02-1234-5678")
                .address("서울시 강남구")
                .detailAddress("1층")
                .minOrderPrice(minOrderPrice)
                .deliveryFee(deliveryFee)
                .status(status)
                .isActive(isActive)
                .build();
    }

    private OrderStatusUpdateRequestDto orderStatusUpdateRequest(OrderStatus status) {
        OrderStatusUpdateRequestDto request = new OrderStatusUpdateRequestDto();
        ReflectionTestUtils.setField(request, "status", status);
        return request;
    }

    private OrderRejectRequestDto rejectRequest(String reason) {
        OrderRejectRequestDto request = new OrderRejectRequestDto();
        ReflectionTestUtils.setField(request, "reason", reason);
        return request;
    }
}
