package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.types.CurrencyType;
import com.example.paymentservice.types.PaymentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

//    @Mock
//    private NotificationService notificationService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void testCreatePayment_success() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE1)
                .amount(new BigDecimal("100"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .details("Payment details")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);  // simulate DB-generated ID
            return p;
        });
//        when(notificationService.notifyExternalService(any(Payment.class))).thenReturn(true);

        Long createdId = paymentService.createPayment(paymentToSave);

        assertNotNull(createdId);
        verify(paymentRepository).save(any(Payment.class));
//        verify(notificationService).notifyExternalService(any(Payment.class));
    }

    @Test
    void testCreatePayment_invalidAmount_throwsException() {
        Payment paymentToSave = Payment.builder()
                .type(PaymentType.TYPE1)
                .amount(new BigDecimal("100"))
                .currency(CurrencyType.USD)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .details("Payment details")
                .build();

        assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(paymentToSave));
    }

    @Test
    void cancelPayment_whenValid_shouldSetCanceledAndReturnFee() {
        Payment payment = Payment.builder()
                .id(1L)
                .type(PaymentType.TYPE1)
                .amount(new BigDecimal("100"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE123")
                .creditorIban("DE321")
                .details("Payment details")
                .createdAt(LocalDateTime.now().minusHours(2))
                .isCanceled(false)
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        BigDecimal fee = paymentService.cancelPayment(1L);

        assertEquals(new BigDecimal("0.10"), fee); // 2h * 0.05
        assertTrue(payment.getIsCanceled());
        assertEquals(fee, payment.getCancellationFee());

        verify(paymentRepository).save(payment);
    }

    @Test
    void cancelPayment_whenNotFound_shouldThrowException() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.cancelPayment(1L));

        assertEquals("Payment not found", ex.getMessage());
    }

    @Test
    void cancelPayment_whenAlreadyCanceled_shouldThrowException() {
        Payment payment = Payment.builder()
                .id(1L)
                .isCanceled(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.cancelPayment(1L));

        assertEquals("Payment is already canceled", ex.getMessage());
    }

    @Test
    void cancelPayment_whenDifferentDay_shouldThrowException() {
        Payment payment = Payment.builder()
                .id(1L)
                .isCanceled(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> paymentService.cancelPayment(1L));

        assertEquals("Payment can only be cancel on the same day", ex.getMessage());
    }
}
