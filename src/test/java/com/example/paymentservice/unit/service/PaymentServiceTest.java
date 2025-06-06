package com.example.paymentservice.unit.service;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.exception.BusinessValidationException;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.service.NotificationServiceImpl;
import com.example.paymentservice.service.PaymentServiceImpl;
import com.example.paymentservice.types.CurrencyType;
import com.example.paymentservice.types.PaymentType;
import com.example.paymentservice.validation.PaymentValidator;
import com.example.paymentservice.validation.PaymentValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private NotificationServiceImpl notificationService;
    private Clock clock;
    private PaymentValidator paymentValidator;
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setup() {
        paymentRepository = mock(PaymentRepository.class);
        notificationService = mock(NotificationServiceImpl.class);
        clock = mock(Clock.class);
        paymentValidator = new PaymentValidatorImpl(clock); // assuming no dependencies
        paymentService = new PaymentServiceImpl(clock, paymentRepository, notificationService, paymentValidator);
    }

    @Test
    void testCreatePayment_success() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE1)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .details("Payment details")
                .build();
        Instant fixedInstant = Instant.parse("2025-01-02T10:15:30.00Z");

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);
            return p;
        });

        Long createdId = paymentService.createPayment(paymentToSave);

        assertNotNull(createdId);
        verify(paymentRepository).save(any(Payment.class));
        verify(notificationService).notifyExternalService(any(Payment.class));
    }

    @Test
    void testCreatePayment_invalidCurrencyForType1_throwsException() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE1)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.USD)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .details("Payment details")
                .build();

        RuntimeException ex = assertThrows(BusinessValidationException.class,
                () -> paymentService.createPayment(paymentToSave));
        assertEquals("Payment of TYPE1 must be EUR", ex.getMessage());
    }

    @Test
    void testCreatePayment_DetailsMissingForType1_throwsException() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE1)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .build();

        RuntimeException ex = assertThrows(BusinessValidationException.class,
                () -> paymentService.createPayment(paymentToSave));
        assertEquals("Details are required for TYPE1 payment", ex.getMessage());
    }

    @Test
    void testCreatePayment_CreditorBankBicProvidedForType1_throwsException() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE1)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .details("Payment details")
                .creditorBankBic("NORZNOZZ77")
                .build();

        RuntimeException ex = assertThrows(BusinessValidationException.class,
                () -> paymentService.createPayment(paymentToSave));
        assertEquals("Creditor Bank BIC is not allowed for TYPE1 payments", ex.getMessage());
    }

    @Test
    void testCreatePayment_CreditorBankBicProvidedForType2_throwsException() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE2)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.USD)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .details("Payment details")
                .creditorBankBic("NORZNOZZ77")
                .build();

        RuntimeException ex = assertThrows(BusinessValidationException.class,
                () -> paymentService.createPayment(paymentToSave));
        assertEquals("Creditor Bank BIC is not allowed for TYPE2 payments", ex.getMessage());
    }

    @Test
    void testCreatePayment_DetailsProvidedForType1_throwsException() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE3)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .details("Payment details")
                .creditorBankBic("NORZNOZZ77")
                .build();

        RuntimeException ex = assertThrows(BusinessValidationException.class,
                () -> paymentService.createPayment(paymentToSave));
        assertEquals("Details are not allowed for TYPE3 payments", ex.getMessage());
    }

    @Test
    void testCreatePayment_invalidCurrencyForType2_throwsException() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE2)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .build();

        RuntimeException ex = assertThrows(BusinessValidationException.class,
                () -> paymentService.createPayment(paymentToSave));
        assertEquals("Payment of TYPE2 must be USD", ex.getMessage());
    }

    @Test
    void testCreatePayment_CreditorBankBICMissingForType3_throwsException() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE3)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .build();

        RuntimeException ex = assertThrows(BusinessValidationException.class,
                () -> paymentService.createPayment(paymentToSave));
        assertEquals("Creditor bank BIC is required for TYPE3 payment", ex.getMessage());
    }

    @Test
    void testCreatePayment_IncorrectAmountScale_throwsException() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE3)
                .amount(new BigDecimal("100.1"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .build();

        RuntimeException ex = assertThrows(BusinessValidationException.class,
                () -> paymentService.createPayment(paymentToSave));
        assertEquals("Monetary amount must have exactly 2 decimal places", ex.getMessage());
    }

    @Test
    void cancelPayment_ValidInputType1_SetCanceledAndReturnFee() {
        Payment payment = Payment.builder()
                .id(1L)
                .type(PaymentType.TYPE1)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE123")
                .creditorIban("DE321")
                .details("Payment details")
                .createdAt(LocalDateTime.of(2025, 1, 2, 8, 10))
                .isCanceled(false)
                .build();
        Instant fixedInstant = Instant.parse("2025-01-02T10:15:30.00Z");

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = paymentService.cancelPayment(1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.10"), result.getCancellationFee()); // 2h * 0.05
        assertTrue(payment.getIsCanceled());

        verify(paymentRepository).save(payment);
    }

    @Test
    void cancelPayment_ValidInputType2_SetCanceledAndReturnFee() {
        Payment payment = Payment.builder()
                .id(1L)
                .type(PaymentType.TYPE2)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE123")
                .creditorIban("DE321")
                .details("Payment details")
                .createdAt(LocalDateTime.of(2025, 1, 2, 7, 10))
                .isCanceled(false)
                .build();
        Instant fixedInstant = Instant.parse("2025-01-02T10:15:30.00Z");

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = paymentService.cancelPayment(1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.30"), result.getCancellationFee()); // 3h * 0.10
        assertTrue(payment.getIsCanceled());

        verify(paymentRepository).save(payment);
    }

    @Test
    void cancelPayment_ValidInputType3_SetCanceledAndReturnFee() {
        Payment payment = Payment.builder()
                .id(1L)
                .type(PaymentType.TYPE3)
                .amount(new BigDecimal("100.00"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE123")
                .creditorIban("DE321")
                .details("Payment details")
                .createdAt(LocalDateTime.of(2025, 1, 2, 5, 40))
                .isCanceled(false)
                .build();
        Instant fixedInstant = Instant.parse("2025-01-02T10:15:30.00Z");

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = paymentService.cancelPayment(1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.60"), result.getCancellationFee()); // 4h * 0.15
        assertTrue(payment.getIsCanceled());
    }

    @Test
    void cancelPayment_PaymentNotFound_ThrowException() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class, () -> paymentService.cancelPayment(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Payment not found", ex.getReason());
    }

    @Test
    void cancelPayment_AlreadyCanceled_ThrowException() {
        Payment payment = Payment.builder()
                .id(1L)
                .isCanceled(true)
                .createdAt(LocalDateTime.of(2025, 1, 2, 0, 0))
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class, () -> paymentService.cancelPayment(1L));
        assertEquals("Payment is already canceled", ex.getMessage());
    }

    @Test
    void cancelPayment_DifferentDay_ThrowException() {
        Payment payment = Payment.builder()
                .id(1L)
                .isCanceled(false)
                .createdAt(LocalDateTime.of(2025, 1, 1, 23, 59))
                .build();
        Instant fixedInstant = Instant.parse("2025-01-02T00:00:00.00Z");

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class, () -> paymentService.cancelPayment(1L));
        assertEquals("Payment can only be cancel on the same day", ex.getMessage());
    }

    @Test
    void getFilteredPayments_WithValueMinAndValueMaxValues_Success() {
        Payment payment = Payment.builder()
                .id(1L)
                .isCanceled(false)
                .createdAt(LocalDateTime.of(2025, 1, 1, 23, 59))
                .build();

        when(paymentRepository.findActivePaymentsWithOptionalMinAmount(BigDecimal.valueOf(10), BigDecimal.valueOf(20)))
                .thenReturn(List.of(payment));

        List<Payment> result = paymentService.getFilteredPayments(BigDecimal.valueOf(10), BigDecimal.valueOf(20));

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getFilteredPayments_WithQueryNulls_Success() {
        Payment payment = Payment.builder()
                .id(1L)
                .isCanceled(false)
                .createdAt(LocalDateTime.of(2025, 1, 1, 23, 59))
                .build();

        when(paymentRepository.findActivePaymentsWithOptionalMinAmount(null, null))
                .thenReturn(List.of(payment));

        List<Payment> result = paymentService.getFilteredPayments(null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
