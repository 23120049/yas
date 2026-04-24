package com.yas.order.model;

import com.yas.order.model.enumeration.CheckoutState;
import com.yas.order.model.enumeration.OrderStatus;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelSmokeTest {

    @Test
    void orderAddress_gettersSettersAndBuilder_areExecutable() {
        OrderAddress address = new OrderAddress();
        address.setId(1L);
        address.setPhone("+84");
        assertEquals(1L, address.getId());
        assertEquals("+84", address.getPhone());

        OrderAddress built = OrderAddress.builder().id(2L).countryName("VN").build();
        assertEquals(2L, built.getId());
        assertEquals("VN", built.getCountryName());
    }

    @Test
    void checkout_builderDefaults_areApplied() {
        Checkout checkout = Checkout.builder().id("c1").email("a@b.com").checkoutState(CheckoutState.PENDING).build();

        assertNotNull(checkout.getTotalAmount());
        assertNotNull(checkout.getTotalShipmentFee());
        assertNotNull(checkout.getTotalShipmentTax());
        assertNotNull(checkout.getTotalDiscountAmount());
        assertNotNull(checkout.getCheckoutItems());
        assertEquals(BigDecimal.ZERO, checkout.getTotalAmount());
        assertTrue(checkout.getCheckoutItems().isEmpty());
    }

    @Test
    void order_gettersSettersAndBuilder_areExecutable() {
        Order order = new Order();
        order.setId(1L);
        order.setEmail("e");
        order.setOrderStatus(OrderStatus.PENDING);
        order.setTotalPrice(new BigDecimal("10.00"));
        order.setCreatedOn(ZonedDateTime.parse("2020-01-01T00:00:00Z"));

        assertEquals(1L, order.getId());
        assertEquals("e", order.getEmail());
        assertEquals(OrderStatus.PENDING, order.getOrderStatus());
        assertEquals(new BigDecimal("10.00"), order.getTotalPrice());
        assertNotNull(order.getCreatedOn());

        Order built = Order.builder().id(2L).email("e2").numberItem(3).build();
        assertEquals(2L, built.getId());
        assertEquals(3, built.getNumberItem());
    }

    @Test
    void checkoutItem_equalsAndHashCode_followIdSemantics() {
        CheckoutItem a = new CheckoutItem();
        a.setId(1L);
        CheckoutItem b = new CheckoutItem();
        b.setId(1L);
        CheckoutItem c = new CheckoutItem();
        c.setId(2L);
        CheckoutItem nullId = new CheckoutItem();

        assertEquals(a, a); // same reference
        assertNotEquals(a, new Object());
        assertEquals(a, b); // same id
        assertNotEquals(a, c); // different id
        assertNotEquals(a, nullId); // null id should not match

        assertEquals(a.hashCode(), b.hashCode()); // hashCode based on class
    }

    @Test
    void orderItem_gettersSettersAndBuilder_areExecutable() {
        OrderItem orderItem = OrderItem.builder()
            .id(1L)
            .productId(2L)
            .orderId(3L)
            .productName("P")
            .quantity(1)
            .productPrice(BigDecimal.ONE)
            .note("note")
            .build();

        assertEquals(1L, orderItem.getId());
        assertEquals(2L, orderItem.getProductId());
        assertEquals(3L, orderItem.getOrderId());
        assertEquals("P", orderItem.getProductName());
        assertEquals(1, orderItem.getQuantity());
        assertEquals(BigDecimal.ONE, orderItem.getProductPrice());
        assertEquals("note", orderItem.getNote());

        orderItem.setDiscountAmount(new BigDecimal("1.00"));
        assertEquals(new BigDecimal("1.00"), orderItem.getDiscountAmount());
    }

    @Test
    void checkoutItem_builderToBuilder_isExecutable() {
        Checkout checkout = Checkout.builder().id("c1").build();
        CheckoutItem item = CheckoutItem.builder().id(1L).productId(2L).quantity(1).checkout(checkout).build();

        CheckoutItem item2 = item.toBuilder().quantity(2).build();
        assertEquals(2, item2.getQuantity());
        assertEquals("c1", item2.getCheckout().getId());
    }

    @Test
    void enums_withName_areCovered() {
        assertEquals("PENDING", com.yas.order.model.enumeration.OrderStatus.PENDING.getName());
        assertNotNull(com.yas.order.model.enumeration.CheckoutState.PENDING.getName());
        assertTrue(List.of(com.yas.order.model.enumeration.PaymentStatus.values()).contains(com.yas.order.model.enumeration.PaymentStatus.PENDING));
    }
}
