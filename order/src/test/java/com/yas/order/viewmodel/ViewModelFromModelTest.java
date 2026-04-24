package com.yas.order.viewmodel;

import com.yas.order.model.Order;
import com.yas.order.model.OrderAddress;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.viewmodel.order.OrderBriefVm;
import com.yas.order.viewmodel.order.OrderGetVm;
import com.yas.order.viewmodel.order.OrderItemGetVm;
import com.yas.order.viewmodel.order.OrderItemVm;
import com.yas.order.viewmodel.order.OrderVm;
import com.yas.order.viewmodel.orderaddress.OrderAddressVm;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViewModelFromModelTest {

    @Test
    void orderAddressVm_fromModel_mapsAllFields() {
        OrderAddress model = OrderAddress.builder()
            .id(1L)
            .contactName("John")
            .phone("+84")
            .addressLine1("a1")
            .addressLine2("a2")
            .city("HCM")
            .zipCode("700000")
            .districtId(10L)
            .districtName("D1")
            .stateOrProvinceId(20L)
            .stateOrProvinceName("HCM")
            .countryId(30L)
            .countryName("VN")
            .build();

        OrderAddressVm vm = OrderAddressVm.fromModel(model);

        assertEquals(1L, vm.id());
        assertEquals("John", vm.contactName());
        assertEquals("+84", vm.phone());
        assertEquals("a1", vm.addressLine1());
        assertEquals("a2", vm.addressLine2());
        assertEquals("HCM", vm.city());
        assertEquals("700000", vm.zipCode());
        assertEquals(10L, vm.districtId());
        assertEquals("D1", vm.districtName());
        assertEquals(20L, vm.stateOrProvinceId());
        assertEquals("HCM", vm.stateOrProvinceName());
        assertEquals(30L, vm.countryId());
        assertEquals("VN", vm.countryName());
    }

    @Test
    void orderItemVms_fromModel_mapsFields() {
        OrderItem orderItem = OrderItem.builder()
            .id(1L)
            .productId(2L)
            .productName("Product")
            .quantity(3)
            .productPrice(new BigDecimal("9.99"))
            .note("note")
            .discountAmount(new BigDecimal("1.00"))
            .taxAmount(new BigDecimal("0.50"))
            .taxPercent(new BigDecimal("10"))
            .orderId(100L)
            .build();

        OrderItemVm vm = OrderItemVm.fromModel(orderItem);

        assertEquals(1L, vm.id());
        assertEquals(2L, vm.productId());
        assertEquals("Product", vm.productName());
        assertEquals(3, vm.quantity());
        assertEquals(new BigDecimal("9.99"), vm.productPrice());
        assertEquals("note", vm.note());
        assertEquals(new BigDecimal("1.00"), vm.discountAmount());
        assertEquals(new BigDecimal("0.50"), vm.taxAmount());
        assertEquals(new BigDecimal("10"), vm.taxPercent());
        assertEquals(100L, vm.orderId());
    }

    @Test
    void orderItemGetVm_fromModels_whenNullOrEmpty_thenReturnEmptyList() {
        assertTrue(OrderItemGetVm.fromModels(null).isEmpty());
        assertTrue(OrderItemGetVm.fromModels(Collections.emptySet()).isEmpty());
    }

    @Test
    void orderVm_fromModel_whenOrderItemsNull_thenOrderItemVmsIsNull() {
        Order order = baseOrder();

        OrderVm vm = OrderVm.fromModel(order, null);

        assertEquals(order.getId(), vm.id());
        assertNull(vm.orderItemVms());
        assertEquals("checkout-1", vm.checkoutId());
    }

    @Test
    void orderVm_fromModel_whenOrderItemsProvided_thenMapsSet() {
        Order order = baseOrder();
        OrderItem orderItem = OrderItem.builder()
            .id(1L)
            .productId(2L)
            .productName("Product")
            .quantity(1)
            .productPrice(BigDecimal.ONE)
            .orderId(order.getId())
            .build();

        OrderVm vm = OrderVm.fromModel(order, Set.of(orderItem));

        assertNotNull(vm.orderItemVms());
        assertEquals(1, vm.orderItemVms().size());
    }

    @Test
    void orderGetVm_fromModel_mapsOrderItemsUsingOrderItemGetVm() {
        Order order = baseOrder();
        order.setCreatedOn(ZonedDateTime.parse("2020-01-01T00:00:00Z"));

        OrderItem orderItem = OrderItem.builder()
            .id(1L)
            .productId(2L)
            .productName("Product")
            .quantity(1)
            .productPrice(BigDecimal.ONE)
            .discountAmount(BigDecimal.ZERO)
            .taxAmount(BigDecimal.ZERO)
            .orderId(order.getId())
            .build();

        OrderGetVm vm = OrderGetVm.fromModel(order, Set.of(orderItem));

        assertEquals(order.getId(), vm.id());
        assertNotNull(vm.orderItems());
        assertEquals(1, vm.orderItems().size());
        assertEquals(order.getCreatedOn(), vm.createdOn());
    }

    @Test
    void orderBriefVm_fromModel_mapsBillingAddressAndCreatedOn() {
        Order order = baseOrder();
        order.setCreatedOn(ZonedDateTime.parse("2020-01-01T00:00:00Z"));

        OrderBriefVm vm = OrderBriefVm.fromModel(order);

        assertEquals(order.getId(), vm.id());
        assertNotNull(vm.billingAddressVm());
        assertEquals("+84", vm.billingAddressVm().phone());
        assertEquals(order.getCreatedOn(), vm.createdOn());
    }

    private static Order baseOrder() {
        OrderAddress shipping = OrderAddress.builder().id(10L).phone("+841").build();
        OrderAddress billing = OrderAddress.builder().id(11L).phone("+84").build();

        return Order.builder()
            .id(100L)
            .email("user@example.com")
            .shippingAddressId(shipping)
            .billingAddressId(billing)
            .note("note")
            .tax(1.0f)
            .discount(2.0f)
            .numberItem(3)
            .totalPrice(new BigDecimal("10.00"))
            .deliveryFee(new BigDecimal("1.00"))
            .couponCode("PROMO")
            .orderStatus(OrderStatus.COMPLETED)
            .deliveryMethod(DeliveryMethod.YAS_EXPRESS)
            .deliveryStatus(DeliveryStatus.PREPARING)
            .paymentStatus(PaymentStatus.COMPLETED)
            .checkoutId("checkout-1")
            .build();
    }
}
