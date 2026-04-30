package com.yas.payment.controller;

import com.yas.payment.service.PaymentService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.CapturePaymentResponseVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentResponseVm;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController controller;

    @Test
    void initPayment_whenServiceReturnsResponse_returnsSameResponse() {
        InitPaymentRequestVm request = Instancio.create(InitPaymentRequestVm.class);
        InitPaymentResponseVm expected = Instancio.create(InitPaymentResponseVm.class);

        lenient().when(paymentService.initPayment(request)).thenReturn(expected);

        InitPaymentResponseVm actual = controller.initPayment(request);

        assertThat(actual).isSameAs(expected);
        verify(paymentService).initPayment(request);
    }

    @Test
    void initPayment_whenServiceThrowsRuntimeException_propagatesException() {
        InitPaymentRequestVm request = Instancio.create(InitPaymentRequestVm.class);
        RuntimeException expected = new IllegalStateException("boom");

        lenient().when(paymentService.initPayment(request)).thenThrow(expected);

        assertThatThrownBy(() -> controller.initPayment(request))
                .isSameAs(expected);

        verify(paymentService).initPayment(request);
    }

    @Test
    void capturePayment_whenServiceReturnsResponse_returnsSameResponse() {
        CapturePaymentRequestVm request = Instancio.create(CapturePaymentRequestVm.class);
        CapturePaymentResponseVm expected = Instancio.create(CapturePaymentResponseVm.class);

        lenient().when(paymentService.capturePayment(request)).thenReturn(expected);

        CapturePaymentResponseVm actual = controller.capturePayment(request);

        assertThat(actual).isSameAs(expected);
        verify(paymentService).capturePayment(request);
    }

    @Test
    void capturePayment_whenServiceThrowsNullPointerException_propagatesException() {
        NullPointerException expected = new NullPointerException("request must not be null");

        lenient().when(paymentService.capturePayment(null)).thenThrow(expected);

        assertThatThrownBy(() -> controller.capturePayment(null))
                .isSameAs(expected);

        verify(paymentService).capturePayment(null);
    }

    @Test
    void cancelPayment_whenCalled_returnsOkWithCancelledMessage() {
        ResponseEntity<String> response = controller.cancelPayment();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo("Payment cancelled");
    }
}
