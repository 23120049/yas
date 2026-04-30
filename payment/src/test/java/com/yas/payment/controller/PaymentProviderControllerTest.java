package com.yas.payment.controller;

import com.yas.payment.service.PaymentProviderService;
import com.yas.payment.viewmodel.paymentprovider.CreatePaymentVm;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import com.yas.payment.viewmodel.paymentprovider.UpdatePaymentVm;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentProviderControllerTest {

    @Mock
    private PaymentProviderService paymentProviderService;

    @InjectMocks
    private PaymentProviderController controller;

    @Test
    void create_whenServiceReturnsVm_returnsCreatedResponse() {
        CreatePaymentVm request = Instancio.create(CreatePaymentVm.class);
        PaymentProviderVm expected = new PaymentProviderVm("paypal", "PayPal", "https://example/config", 1, 10L, "https://example/icon");

        lenient().when(paymentProviderService.create(request)).thenReturn(expected);

        ResponseEntity<PaymentProviderVm> response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(expected);
        verify(paymentProviderService).create(request);
    }

    @Test
    void create_whenServiceThrowsRuntimeException_propagatesException() {
        CreatePaymentVm request = Instancio.create(CreatePaymentVm.class);
        RuntimeException expected = new IllegalArgumentException("invalid");

        lenient().when(paymentProviderService.create(request)).thenThrow(expected);

        assertThatThrownBy(() -> controller.create(request)).isSameAs(expected);
        verify(paymentProviderService).create(request);
    }

    @Test
    void update_whenServiceReturnsVm_returnsOkResponse() {
        UpdatePaymentVm request = Instancio.create(UpdatePaymentVm.class);
        PaymentProviderVm expected = new PaymentProviderVm("paypal", "PayPal", "https://example/config", 2, 10L, "https://example/icon");

        lenient().when(paymentProviderService.update(request)).thenReturn(expected);

        ResponseEntity<PaymentProviderVm> response = controller.update(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(paymentProviderService).update(request);
    }

    @Test
    void getAll_whenServiceReturnsList_returnsOkResponseWithBody() {
        Pageable pageable = Pageable.unpaged();
        List<PaymentProviderVm> expected = List.of(new PaymentProviderVm("paypal", "PayPal", "https://example/config", 1, 10L, "https://example/icon"));

        lenient().when(paymentProviderService.getEnabledPaymentProviders(pageable)).thenReturn(expected);

        ResponseEntity<List<PaymentProviderVm>> response = controller.getAll(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(expected);
        verify(paymentProviderService).getEnabledPaymentProviders(pageable);
    }
}
