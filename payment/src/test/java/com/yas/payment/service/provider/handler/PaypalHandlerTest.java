package com.yas.payment.service.provider.handler;

import com.yas.payment.model.CapturedPayment;
import com.yas.payment.model.InitiatedPayment;
import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.paypal.service.PaypalService;
import com.yas.payment.paypal.viewmodel.PaypalCapturePaymentRequest;
import com.yas.payment.paypal.viewmodel.PaypalCapturePaymentResponse;
import com.yas.payment.paypal.viewmodel.PaypalCreatePaymentRequest;
import com.yas.payment.paypal.viewmodel.PaypalCreatePaymentResponse;
import com.yas.payment.service.PaymentProviderService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaypalHandlerTest {

    @Mock
    private PaymentProviderService paymentProviderService;

    @Mock
    private PaypalService paypalService;

    private PaypalHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PaypalHandler(paymentProviderService, paypalService);
    }

    @Test
    void getProviderId_whenCalled_returnsPaypalProviderName() {
        assertThat(handler.getProviderId()).isEqualTo(PaymentMethod.PAYPAL.name());
    }

    @Test
    void initPayment_whenPaypalServiceReturnsResponse_mapsToInitiatedPayment() {
        InitPaymentRequestVm requestVm = InitPaymentRequestVm.builder()
                .paymentMethod(PaymentMethod.PAYPAL.name())
                .totalPrice(new BigDecimal("12.34"))
                .checkoutId("checkout-1")
                .build();
        String paymentSettings = "settings";
        PaypalCreatePaymentResponse paypalResponse = PaypalCreatePaymentResponse.builder()
                .status("CREATED")
                .paymentId("payment-123")
                .redirectUrl("https://paypal/redirect")
                .build();

        lenient().when(paymentProviderService.getAdditionalSettingsByPaymentProviderId(PaymentMethod.PAYPAL.name()))
                .thenReturn(paymentSettings);
        lenient().when(paypalService.createPayment(any(PaypalCreatePaymentRequest.class)))
                .thenReturn(paypalResponse);

        InitiatedPayment initiatedPayment = handler.initPayment(requestVm);

        assertThat(initiatedPayment.getStatus()).isEqualTo("CREATED");
        assertThat(initiatedPayment.getPaymentId()).isEqualTo("payment-123");
        assertThat(initiatedPayment.getRedirectUrl()).isEqualTo("https://paypal/redirect");

        ArgumentCaptor<PaypalCreatePaymentRequest> requestCaptor = ArgumentCaptor.forClass(PaypalCreatePaymentRequest.class);
        verify(paypalService).createPayment(requestCaptor.capture());
        PaypalCreatePaymentRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.totalPrice()).isEqualByComparingTo(new BigDecimal("12.34"));
        assertThat(capturedRequest.checkoutId()).isEqualTo("checkout-1");
        assertThat(capturedRequest.paymentMethod()).isEqualTo(PaymentMethod.PAYPAL.name());
        assertThat(capturedRequest.paymentSettings()).isEqualTo(paymentSettings);

        verify(paymentProviderService).getAdditionalSettingsByPaymentProviderId(PaymentMethod.PAYPAL.name());
    }

    @Test
    void initPayment_whenPaypalServiceThrowsRuntimeException_propagatesException() {
        InitPaymentRequestVm requestVm = InitPaymentRequestVm.builder()
                .paymentMethod(PaymentMethod.PAYPAL.name())
                .totalPrice(BigDecimal.ONE)
                .checkoutId("checkout-1")
                .build();
        RuntimeException expected = new IllegalStateException("boom");

        lenient().when(paymentProviderService.getAdditionalSettingsByPaymentProviderId(PaymentMethod.PAYPAL.name()))
                .thenReturn("settings");
        lenient().when(paypalService.createPayment(any(PaypalCreatePaymentRequest.class)))
                .thenThrow(expected);

        assertThatThrownBy(() -> handler.initPayment(requestVm)).isSameAs(expected);

        verify(paymentProviderService).getAdditionalSettingsByPaymentProviderId(PaymentMethod.PAYPAL.name());
        verify(paypalService).createPayment(any(PaypalCreatePaymentRequest.class));
    }

    @Test
    void capturePayment_whenPaypalServiceReturnsResponse_mapsToCapturedPayment() {
        CapturePaymentRequestVm requestVm = CapturePaymentRequestVm.builder()
                .paymentMethod(PaymentMethod.PAYPAL.name())
                .token("token-abc")
                .build();
        String paymentSettings = "settings";
        PaypalCapturePaymentResponse paypalResponse = PaypalCapturePaymentResponse.builder()
                .checkoutId("checkout-1")
                .amount(new BigDecimal("100.00"))
                .paymentFee(new BigDecimal("1.23"))
                .gatewayTransactionId("txn-1")
                .paymentMethod(PaymentMethod.PAYPAL.name())
                .paymentStatus(PaymentStatus.COMPLETED.name())
                .failureMessage(null)
                .build();

        lenient().when(paymentProviderService.getAdditionalSettingsByPaymentProviderId(PaymentMethod.PAYPAL.name()))
                .thenReturn(paymentSettings);
        lenient().when(paypalService.capturePayment(any(PaypalCapturePaymentRequest.class)))
                .thenReturn(paypalResponse);

        CapturedPayment capturedPayment = handler.capturePayment(requestVm);

        assertThat(capturedPayment.getCheckoutId()).isEqualTo("checkout-1");
        assertThat(capturedPayment.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(capturedPayment.getPaymentFee()).isEqualByComparingTo(new BigDecimal("1.23"));
        assertThat(capturedPayment.getGatewayTransactionId()).isEqualTo("txn-1");
        assertThat(capturedPayment.getPaymentMethod()).isEqualTo(PaymentMethod.PAYPAL);
        assertThat(capturedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(capturedPayment.getFailureMessage()).isNull();

        ArgumentCaptor<PaypalCapturePaymentRequest> requestCaptor = ArgumentCaptor.forClass(PaypalCapturePaymentRequest.class);
        verify(paypalService).capturePayment(requestCaptor.capture());
        PaypalCapturePaymentRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.token()).isEqualTo("token-abc");
        assertThat(capturedRequest.paymentSettings()).isEqualTo(paymentSettings);

        verify(paymentProviderService).getAdditionalSettingsByPaymentProviderId(PaymentMethod.PAYPAL.name());
    }
}
