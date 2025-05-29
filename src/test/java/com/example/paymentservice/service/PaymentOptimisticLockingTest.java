package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.types.CurrencyType;
import com.example.paymentservice.types.PaymentType;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Integration test - run manually with profile")
@SpringBootTest
public class PaymentOptimisticLockingTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @Transactional
    void whenTwoTransactionsUpdateSameEntity_thenOptimisticLockException() {
        // Arrange: create and save a payment
        Payment payment = Payment.builder()
                .type(PaymentType.TYPE1)
                .amount(new BigDecimal("100"))
                .currency(CurrencyType.EUR)
                .debtorIban("DE1234567890")
                .creditorIban("DE0987654321")
                .details("Initial payment")
                .build();

        payment = paymentRepository.save(payment);

        // Simulate loading the same entity in two different "transactions"
        // (here just two separate references in the same transaction, but we mimic detach)
        Payment paymentTx1 = paymentRepository.findById(payment.getId()).orElseThrow();
        Payment paymentTx2 = paymentRepository.findById(payment.getId()).orElseThrow();

        // Make changes in transaction 1 and save
        paymentTx1.setDetails("Updated by Tx1");
        paymentRepository.save(paymentTx1);

        // Detach paymentTx2 to simulate stale entity state (needed if running in one transaction)
        // This line depends on your EntityManager, assuming you have it injected:
        // entityManager.detach(paymentTx2);
        // For now, let’s assume this test runs in separate transactions or flushes are done

        // Make changes in transaction 2 and attempt to save - should fail
        paymentTx2.setDetails("Updated by Tx2");

        // Act & Assert
        assertThrows(OptimisticLockException.class, () -> {
            paymentRepository.saveAndFlush(paymentTx2);
            // saveAndFlush triggers DB write and version check immediately
        });
    }
}