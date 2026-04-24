package com.yas.order.mapper;

import com.yas.order.model.Checkout;
import com.yas.order.model.CheckoutItem;
import com.yas.order.viewmodel.checkout.CheckoutItemPostVm;
import com.yas.order.viewmodel.checkout.CheckoutPostVm;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import static org.instancio.Select.field;
import org.junit.jupiter.api.Test;

class CheckoutMapperTest {

    private final CheckoutMapper checkoutMapper = new CheckoutMapperImpl();

    @Test
    void testCheckoutItemPostVmToModel_convertToCorrectCheckoutItem() {

        var src = Instancio.create(CheckoutItemPostVm.class);

        var res = checkoutMapper.toModel(src);

        Assertions.assertThat(res)
                .hasFieldOrPropertyWithValue("productId", src.productId())
                .hasFieldOrPropertyWithValue("quantity", src.quantity())
                .hasFieldOrPropertyWithValue("description", src.description());
    }

    @Test
    void testCheckoutPostVmToModel_convertToCorrectCheckout() {

        CheckoutPostVm checkoutPostVm = Instancio.of(CheckoutPostVm.class)
                .supply(field(CheckoutPostVm.class, "shippingAddressId"), gen -> Long.toString(gen.longRange(1, 10000)))
                .create();
        var res = checkoutMapper.toModel(checkoutPostVm);

        Assertions.assertThat(res)
                .hasFieldOrPropertyWithValue("email", checkoutPostVm.email())
                .hasFieldOrPropertyWithValue("note", checkoutPostVm.note())
                .hasFieldOrPropertyWithValue("promotionCode", checkoutPostVm.promotionCode())
                .hasFieldOrPropertyWithValue("shipmentMethodId", checkoutPostVm.shipmentMethodId())
                .hasFieldOrPropertyWithValue("paymentMethodId", checkoutPostVm.paymentMethodId())
                .hasFieldOrPropertyWithValue("shippingAddressId", Long.valueOf(checkoutPostVm.shippingAddressId()));

    }

    @Test
    void testCheckoutToVm_convertToCheckoutVmCorrectly() {

        Checkout checkout = Checkout.builder()
            .id("checkout-1")
            .email("user@example.com")
            .note("note")
            .promotionCode("PROMO")
            .shipmentMethodId("ship-1")
            .paymentMethodId("pay-1")
            .shippingAddressId(10L)
            .totalAmount(new BigDecimal("100.00"))
            .totalShipmentFee(new BigDecimal("5.00"))
            .totalShipmentTax(new BigDecimal("0.50"))
            .totalTax(new BigDecimal("2.00"))
            .totalDiscountAmount(new BigDecimal("1.00"))
            .checkoutItems(List.of())
            .build();

        var res = checkoutMapper.toVm(checkout);

        Assertions.assertThat(res).hasFieldOrPropertyWithValue("id", checkout.getId())
                .hasFieldOrPropertyWithValue("email", checkout.getEmail())
                .hasFieldOrPropertyWithValue("note", checkout.getNote())
                .hasFieldOrPropertyWithValue("promotionCode", checkout.getPromotionCode())
                .hasFieldOrPropertyWithValue("shipmentMethodId", checkout.getShipmentMethodId())
                .hasFieldOrPropertyWithValue("paymentMethodId", checkout.getPaymentMethodId())
                .hasFieldOrPropertyWithValue("shippingAddressId", checkout.getShippingAddressId());

        Assertions.assertThat(res.checkoutItemVms()).isNull();
    }

    @Test
    void testCheckoutItemToVm_convertCheckoutItemCorrectly() {

        Checkout checkout = Checkout.builder().id("checkout-123").build();
        CheckoutItem checkoutItem = CheckoutItem.builder()
            .id(1L)
            .productId(101L)
            .productName("Product")
            .description("Desc")
            .quantity(2)
            .productPrice(new BigDecimal("9.99"))
            .taxAmount(new BigDecimal("1.23"))
            .discountAmount(new BigDecimal("0.50"))
            .shipmentFee(new BigDecimal("2.00"))
            .shipmentTax(new BigDecimal("0.20"))
            .checkout(checkout)
            .build();

        var res = checkoutMapper.toVm(checkoutItem);

        Assertions.assertThat(res)
                .hasFieldOrPropertyWithValue("id", checkoutItem.getId())
                .hasFieldOrPropertyWithValue("productId", checkoutItem.getProductId())
                .hasFieldOrPropertyWithValue("productName", checkoutItem.getProductName())
                .hasFieldOrPropertyWithValue("description", checkoutItem.getDescription())
                .hasFieldOrPropertyWithValue("quantity", checkoutItem.getQuantity())
                .hasFieldOrPropertyWithValue("productPrice", checkoutItem.getProductPrice())
                .hasFieldOrPropertyWithValue("taxAmount", checkoutItem.getTaxAmount())
                .hasFieldOrPropertyWithValue("discountAmount", checkoutItem.getDiscountAmount())
                .hasFieldOrPropertyWithValue("shipmentFee", checkoutItem.getShipmentFee())
                .hasFieldOrPropertyWithValue("shipmentTax", checkoutItem.getShipmentTax())
                .hasFieldOrPropertyWithValue("checkoutId", checkoutItem.getCheckout().getId());
    }

    @Test
    void map_whenNull_thenReturnZero() {
        Assertions.assertThat(checkoutMapper.map(null)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void map_whenNotNull_thenReturnSameValue() {
        BigDecimal value = new BigDecimal("12.34");
        Assertions.assertThat(checkoutMapper.map(value)).isSameAs(value);
    }
}
