package com.example.paymentservice.validation;

import com.example.paymentservice.entity.Payment;

import java.math.BigDecimal;

public interface PaymentValidator {
    void validateNewPayment(Payment payment);

    void validateFilteredPaymentsQuery(BigDecimal amountMin, BigDecimal amountMax);

    void validatePaymentToBeCancelled(Payment payment);
}
