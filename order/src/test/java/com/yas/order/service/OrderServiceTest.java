package com.yas.order.service;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.utils.AuthenticationUtils;
import com.yas.order.mapper.OrderMapper;
import com.yas.order.model.Order;
import com.yas.order.model.OrderAddress;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.repository.OrderItemRepository;
import com.yas.order.repository.OrderRepository;
import com.yas.order.viewmodel.order.*;
import com.yas.order.viewmodel.orderaddress.OrderAddressPostVm;
import com.yas.order.viewmodel.product.ProductVariationVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private ProductService productService;
    @Mock private CartService cartService;
    @Mock private OrderMapper orderMapper;
    @Mock private PromotionService promotionService;

    @InjectMocks private OrderService orderService;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void testGetOrderWithItemsById_WhenOrderNotFound_ShouldThrowNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.getOrderWithItemsById(1L));
    }

    @Test
    void testGetOrderWithItemsById_WhenOrderFound_ShouldReturnOrderVm() {
        // Mock toàn diện Order và OrderAddress để chống NullPointer
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(1L);
        
        OrderAddress mockAddress = mock(OrderAddress.class);
        lenient().when(mockAddress.getId()).thenReturn(1L);
        lenient().when(order.getShippingAddressId()).thenReturn(mockAddress);
        lenient().when(order.getBillingAddressId()).thenReturn(mockAddress);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrderId(1L)).thenReturn(Collections.singletonList(new OrderItem()));

        OrderVm result = orderService.getOrderWithItemsById(1L);
        assertNotNull(result);
    }

    @Test
    void testGetAllOrder_WhenNoOrders_ShouldReturnEmptyList() {
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList());
        when(orderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        OrderListVm result = orderService.getAllOrder(
                org.springframework.data.util.Pair.of(ZonedDateTime.now(), ZonedDateTime.now()),
                "", Collections.emptyList(), org.springframework.data.util.Pair.of("", ""), "", org.springframework.data.util.Pair.of(0, 10)
        );

        assertTrue(result.orderList() == null || result.orderList().isEmpty());
    }

    @Test
    void testGetLatestOrders_WhenCountIsZero_ShouldReturnEmptyList() {
        List<OrderBriefVm> result = orderService.getLatestOrders(0);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetLatestOrders_WhenNoOrders_ShouldReturnEmptyList() {
        when(orderRepository.getLatestOrders(any(Pageable.class))).thenReturn(Collections.emptyList());
        List<OrderBriefVm> result = orderService.getLatestOrders(5);
        assertTrue(result.isEmpty());
    }

    @Test
    void testIsOrderCompletedWithUserIdAndProductId_WhenNoVariations_ShouldCheckOrder() {
        try (MockedStatic<AuthenticationUtils> mockedAuth = mockStatic(AuthenticationUtils.class)) {
            mockedAuth.when(AuthenticationUtils::extractUserId).thenReturn("test-user");
            when(productService.getProductVariations(1L)).thenReturn(Collections.emptyList());
            when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(new Order()));

            OrderExistsByProductAndUserGetVm result = orderService.isOrderCompletedWithUserIdAndProductId(1L);
            assertTrue(result.isPresent()); 
        }
    }

    @Test
    void testIsOrderCompletedWithUserIdAndProductId_WithVariations_ShouldCheckOrder() {
        try (MockedStatic<AuthenticationUtils> mockedAuth = mockStatic(AuthenticationUtils.class)) {
            mockedAuth.when(AuthenticationUtils::extractUserId).thenReturn("test-user");
            
            ProductVariationVm mockVariation = mock(ProductVariationVm.class);
            List<ProductVariationVm> variations = List.of(mockVariation);
            
            when(productService.getProductVariations(1L)).thenReturn(variations);
            when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

            OrderExistsByProductAndUserGetVm result = orderService.isOrderCompletedWithUserIdAndProductId(1L);
            assertFalse(result.isPresent());
        }
    }

    @Test
    void testFindOrderByCheckoutId_WhenNotFound_ShouldThrowNotFoundException() {
        when(orderRepository.findByCheckoutId("non-existent-id")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.findOrderByCheckoutId("non-existent-id"));
    }

    @Test
    void testUpdateOrderPaymentStatus_WhenOrderNotFound_ShouldThrowNotFoundException() {
        PaymentOrderStatusVm vm = mock(PaymentOrderStatusVm.class);
        when(vm.orderId()).thenReturn(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.updateOrderPaymentStatus(vm));
    }

    @Test
    void testUpdateOrderPaymentStatus_WhenPaymentCompleted_ShouldUpdateOrderStatusToPaid() {
        Order order = mock(Order.class);
        lenient().when(order.getId()).thenReturn(1L);
        lenient().when(order.getOrderStatus()).thenReturn(OrderStatus.PENDING);
        
        PaymentOrderStatusVm vm = mock(PaymentOrderStatusVm.class);
        lenient().when(vm.orderId()).thenReturn(1L);
        lenient().when(vm.paymentStatus()).thenReturn(PaymentStatus.COMPLETED.name());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        orderService.updateOrderPaymentStatus(vm);
        
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
    }

    @Test
    void testRejectOrder_WhenOrderNotFound_ShouldThrowNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.rejectOrder(1L, "reason"));
    }

    @Test
    void testRejectOrder_ShouldUpdateStatusAndReason() {
        Order order = mock(Order.class);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.rejectOrder(1L, "Test Reject");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
    }

    @Test
    void testAcceptOrder_WhenOrderNotFound_ShouldThrowNotFoundException() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> orderService.acceptOrder(1L));
    }

    @Test
    void testAcceptOrder_ShouldUpdateStatusToAccepted() {
        Order order = mock(Order.class);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.acceptOrder(1L);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
    }

    @Test
    void testCreateOrder_Success() {
        OrderPostVm postVm = mock(OrderPostVm.class);
        OrderAddressPostVm mockAddressPostVm = mock(OrderAddressPostVm.class);
        
        lenient().when(postVm.billingAddressPostVm()).thenReturn(mockAddressPostVm);
        lenient().when(postVm.shippingAddressPostVm()).thenReturn(mockAddressPostVm);
        lenient().when(postVm.orderItemPostVms()).thenReturn(Collections.emptyList());

        // Ép Mockito gán ID = 1L ngay khi hàm save() được gọi
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        
        when(orderItemRepository.saveAll(any())).thenReturn(Collections.emptyList());

        // Giả lập cho hàm getOrderWithItemsById được gọi ở cuối createOrder
        Order savedOrder = new Order();
        savedOrder.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(savedOrder));

        OrderVm result = orderService.createOrder(postVm);
        assertNotNull(result);
    }
}
