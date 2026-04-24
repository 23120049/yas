package com.yas.order.mapper;

import com.yas.order.model.csv.OrderItemCsv;
import com.yas.order.viewmodel.order.OrderBriefVm;
import com.yas.order.viewmodel.orderaddress.OrderAddressVm;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapperImpl();

    @Test
    void toCsv_mapsIdAndBillingPhoneCorrectly() {
        var billingAddress = OrderAddressVm.builder()
            .id(1L)
            .phone("+841234")
            .build();

        OrderBriefVm src = OrderBriefVm.builder()
            .id(10L)
            .email("user@example.com")
            .billingAddressVm(billingAddress)
            .totalPrice(new BigDecimal("123.45"))
            .createdOn(ZonedDateTime.parse("2020-01-01T00:00:00Z"))
            .build();

        OrderItemCsv csv = orderMapper.toCsv(src);

        Assertions.assertThat(csv.getId()).isEqualTo(10L);
        Assertions.assertThat(csv.getPhone()).isEqualTo("+841234");
        Assertions.assertThat(csv.getEmail()).isEqualTo("user@example.com");
        Assertions.assertThat(csv.getTotalPrice()).isEqualByComparingTo("123.45");
    }
}
