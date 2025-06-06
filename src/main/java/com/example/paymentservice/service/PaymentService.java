package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentService {
    Long createPayment(Payment payment);
    Payment getPayment(Long id);
    Payment cancelPayment(Long id);
    List<Payment> getFilteredPayments(BigDecimal amountMin, BigDecimal amountMax);
}
