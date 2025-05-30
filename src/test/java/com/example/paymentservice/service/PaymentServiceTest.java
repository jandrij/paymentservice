package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.types.CurrencyType;
import com.example.paymentservice.types.PaymentType;
import com.example.paymentservice.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

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
                .createdAt(LocalDateTime.now().minusHours(2).minusMinutes(59))
                .isCanceled(false)
                .build();

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
                .createdAt(LocalDateTime.now().minusHours(3).minusMinutes(59))
                .isCanceled(false)
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = paymentService.cancelPayment(1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.30"), result.getCancellationFee()); // 2h * 0.05
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
                .createdAt(LocalDateTime.now().minusHours(4).minusMinutes(59))
                .isCanceled(false)
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = paymentService.cancelPayment(1L);

        assertNotNull(result);
        assertEquals(new BigDecimal("0.60"), result.getCancellationFee()); // 2h * 0.05
        assertTrue(payment.getIsCanceled());
    }

    @Test
    void cancelPayment_PaymentNotFound_ThrowException() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.cancelPayment(1L));
        assertEquals("Payment not found", ex.getMessage());
    }

    @Test
    void cancelPayment_AlreadyCanceled_ThrowException() {
        Payment payment = Payment.builder()
                .id(1L)
                .isCanceled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        RuntimeException ex = assertThrows(BusinessValidationException.class, () -> paymentService.cancelPayment(1L));
        assertEquals("Payment is already canceled", ex.getMessage());
    }

    @Test
    void cancelPayment_DifferentDay_ThrowException() {
        Payment payment = Payment.builder()
                .id(1L)
                .isCanceled(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        RuntimeException ex = assertThrows(BusinessValidationException.class, () -> paymentService.cancelPayment(1L));
        assertEquals("Payment can only be cancel on the same day", ex.getMessage());
    }
}
