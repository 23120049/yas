package com.yas.order.viewmodel;

import com.yas.order.model.enumeration.CheckoutState;
import com.yas.order.model.enumeration.DeliveryMethod;
import com.yas.order.model.enumeration.DeliveryStatus;
import com.yas.order.model.enumeration.OrderStatus;
import com.yas.order.model.enumeration.PaymentMethod;
import com.yas.order.model.enumeration.PaymentStatus;
import com.yas.order.viewmodel.cart.CartItemDeleteVm;
import com.yas.order.viewmodel.checkout.CheckoutItemPostVm;
import com.yas.order.viewmodel.checkout.CheckoutItemVm;
import com.yas.order.viewmodel.checkout.CheckoutPaymentMethodPutVm;
import com.yas.order.viewmodel.checkout.CheckoutPostVm;
import com.yas.order.viewmodel.checkout.CheckoutStatusPutVm;
import com.yas.order.viewmodel.checkout.CheckoutVm;
import com.yas.order.viewmodel.customer.CustomerVm;
import com.yas.order.viewmodel.order.OrderExistsByProductAndUserGetVm;
import com.yas.order.viewmodel.order.OrderGetVm;
import com.yas.order.viewmodel.order.OrderBriefVm;
import com.yas.order.viewmodel.order.OrderItemGetVm;
import com.yas.order.viewmodel.order.OrderItemPostVm;
import com.yas.order.viewmodel.order.OrderItemVm;
import com.yas.order.viewmodel.order.OrderListVm;
import com.yas.order.viewmodel.order.OrderPostVm;
import com.yas.order.viewmodel.order.OrderVm;
import com.yas.order.viewmodel.order.PaymentOrderStatusVm;
import com.yas.order.viewmodel.orderaddress.OrderAddressPostVm;
import com.yas.order.viewmodel.orderaddress.OrderAddressVm;
import com.yas.order.viewmodel.product.ProductCheckoutListVm;
import com.yas.order.viewmodel.product.ProductGetCheckoutListVm;
import com.yas.order.viewmodel.product.ProductQuantityItem;
import com.yas.order.viewmodel.product.ProductVariationVm;
import com.yas.order.viewmodel.promotion.PromotionUsageVm;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViewModelSmokeTest {

    @Test
    void records_accessors_equals_hashCode_toString_areExecutable() {
        var itemPost = new CheckoutItemPostVm(1L, "desc", 2);
        assertEquals(1L, itemPost.productId());
        assertNotNull(itemPost.toString());

        var checkoutPost = new CheckoutPostVm("a@b.com", "note", "PROMO", "ship", "pay", "10", List.of(itemPost));
        assertEquals("a@b.com", checkoutPost.email());

        var statusPut = new CheckoutStatusPutVm("c1", "PENDING");
        assertEquals("c1", statusPut.checkoutId());

        var paymentPut = new CheckoutPaymentMethodPutVm("PAYPAL");
        assertEquals("PAYPAL", paymentPut.paymentMethodId());

        var checkoutItemVm = new CheckoutItemVm(1L, 2L, "name", "desc", 3,
            new BigDecimal("9.99"), new BigDecimal("1.00"), new BigDecimal("0.50"),
            new BigDecimal("2.00"), new BigDecimal("0.20"), "checkout-1");
        assertEquals(2L, checkoutItemVm.productId());

        Set<CheckoutItemVm> checkoutItemVms = new HashSet<>();
        checkoutItemVms.add(checkoutItemVm);

        var checkoutVm = new CheckoutVm("checkout-1", "a@b.com", "note", "PROMO", CheckoutState.PENDING,
            "progress", BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
            "ship", "pay", 10L, checkoutItemVms);
        assertEquals("checkout-1", checkoutVm.id());

        var customerVm = new CustomerVm("u", "e", "f", "l");
        assertEquals("u", customerVm.username());

        var errorVm = new ErrorVm("400", "Bad", "Detail");
        assertNotNull(errorVm.fieldErrors());
        assertEquals(0, errorVm.fieldErrors().size());

        var responseStatus = new ResponeStatusVm("t", "m", "200");
        assertEquals("200", responseStatus.statusCode());

        var promotionUsage = new PromotionUsageVm("CODE", 2L, "user", 3L);
        assertEquals("CODE", promotionUsage.promotionCode());

        var productVariation = new ProductVariationVm(1L, "n", "sku");
        assertEquals("sku", productVariation.sku());

        var productQuantity = new ProductQuantityItem(1L, 2L);
        assertEquals(2L, productQuantity.quantity());

        var productCheckout = ProductCheckoutListVm.builder().id(1L).name("p").price(1.5).taxClassId(2L).build();
        assertEquals("p", productCheckout.getName());
        productCheckout.setName("p2");
        assertEquals("p2", productCheckout.getName());
        assertNotNull(productCheckout.toString());

        var productGetCheckout = new ProductGetCheckoutListVm(List.of(productCheckout), 0, 10, 1, 1, true);
        assertTrue(productGetCheckout.isLast());

        var addressVm = OrderAddressVm.builder().id(1L).phone("+84").countryName("VN").build();
        assertEquals("+84", addressVm.phone());

        var addressPostVm = new OrderAddressPostVm("c", "p", "a1", "a2", "city", "zip",
            1L, "d", 2L, "s", 3L, "country");
        assertEquals("city", addressPostVm.city());

        var orderItemVm = OrderItemVm.builder().id(1L).productId(2L).productName("n").quantity(1)
            .productPrice(BigDecimal.ONE).note("note").discountAmount(BigDecimal.ZERO)
            .taxAmount(BigDecimal.ZERO).taxPercent(BigDecimal.ZERO).orderId(10L).build();
        assertEquals(10L, orderItemVm.orderId());

        var orderVm = OrderVm.builder().id(1L).email("a@b.com")
            .shippingAddressVm(addressVm).billingAddressVm(addressVm)
            .note("note").tax(1.0f).discount(2.0f).numberItem(1)
            .totalPrice(BigDecimal.TEN).deliveryFee(BigDecimal.ONE).couponCode("C")
            .orderStatus(OrderStatus.COMPLETED).deliveryMethod(DeliveryMethod.YAS_EXPRESS)
            .deliveryStatus(DeliveryStatus.PREPARING).paymentStatus(PaymentStatus.PENDING)
            .orderItemVms(Set.of(orderItemVm)).checkoutId("ck").build();
        assertEquals("ck", orderVm.checkoutId());

        var orderItemPost = new OrderItemPostVm(1L, "n", 1, BigDecimal.ONE, "note",
            BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals("n", orderItemPost.productName());

        var orderPost = new OrderPostVm("checkoutId", "a@b.com", addressPostVm, addressPostVm,
            "note", 1.0f, 2.0f, 1, BigDecimal.TEN, BigDecimal.ONE, "COUPON",
            DeliveryMethod.GRAB_EXPRESS, PaymentMethod.COD, PaymentStatus.PENDING, List.of(orderItemPost));
        assertEquals("checkoutId", orderPost.checkoutId());

        var orderBrief = OrderBriefVm.builder().id(1L).email("a@b.com").billingAddressVm(addressVm).build();
        var orderList = new OrderListVm(List.of(orderBrief), 1L, 1);
        assertEquals(1, orderList.totalPages());

        var paymentOrderStatus = new PaymentOrderStatusVm(1L, "Completed", 2L, "Paid");
        assertEquals("Paid", paymentOrderStatus.paymentStatus());

        var existsVm = new OrderExistsByProductAndUserGetVm(true);
        assertTrue(existsVm.isPresent());

        var orderItemGetVm = new OrderItemGetVm(1L, 2L, "n", 1, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(1, orderItemGetVm.quantity());

        var orderGetVm = new OrderGetVm(1L, OrderStatus.COMPLETED, BigDecimal.TEN, DeliveryStatus.DELIVERED,
            DeliveryMethod.YAS_EXPRESS, List.of(orderItemGetVm), ZonedDateTime.parse("2020-01-01T00:00:00Z"));
        assertEquals(1L, orderGetVm.id());

        var cartDelete = new CartItemDeleteVm(1L, 2);
        assertEquals(1L, cartDelete.productId());
        assertEquals(2, cartDelete.quantity());

        // equals/hashCode smoke for records
        assertEquals(orderItemGetVm, new OrderItemGetVm(1L, 2L, "n", 1, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO));
        assertEquals(orderItemGetVm.hashCode(), new OrderItemGetVm(1L, 2L, "n", 1, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO).hashCode());

        // Ensure canonical methods are callable
        assertNotNull(new ArrayList<>(checkoutItemVms).get(0).toString());
        assertNotNull(orderVm.toString());
    }

    @Test
    void builder_toBuilder_areExecutable_forLombokAnnotatedTypes() {
        var original = CheckoutItemVm.builder().id(1L).productId(2L).productName("p").quantity(1).build();
        assertEquals(2L, original.productId());

        var checkoutVm = CheckoutVm.builder().id("c").email("e").checkoutState(CheckoutState.PENDING).build();
        var mutated = checkoutVm.toBuilder().promotionCode("PROMO").build();
        assertEquals("PROMO", mutated.promotionCode());

        var product = ProductCheckoutListVm.builder().id(1L).name("p").build();
        var product2 = product.toBuilder().name("p2").build();
        assertEquals("p2", product2.getName());
    }
}
