package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.validation.PaymentValidator;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final Clock clock;
    private final PaymentRepository repo;
    private final NotificationService notificationService;
    private final PaymentValidator paymentValidator;

    public Long createPayment(Payment payment) {
        paymentValidator.validateNewPayment(payment);
        payment.setCreatedAt(LocalDateTime.now(clock));
        payment.setIsCanceled(Boolean.FALSE);
        Payment savedPayment = repo.save(payment);
        notificationService.notifyExternalService(savedPayment);
        return savedPayment.getId();
    }

    public Payment getPayment(Long id) {
        return repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @Transactional
    public Payment cancelPayment(Long id) {
        try {
            Payment payment = repo.findById(id).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
            paymentValidator.validatePaymentToBeCancelled(payment);
            payment.setIsCanceled(Boolean.TRUE);
            BigDecimal fee = calculateCancellationFee(payment);
            payment.setCancellationFee(fee);
            return repo.save(payment);
        } catch (OptimisticLockException ex) {
            throw new RuntimeException("Payment was modified concurrently. Please retry.");
        }
    }

    public BigDecimal calculateCancellationFee(Payment payment) {
        long hours = Duration.between(payment.getCreatedAt(), LocalDateTime.now(clock)).toHours();
        BigDecimal coefficient = switch (payment.getType()) {
            case TYPE1 -> BigDecimal.valueOf(0.05);
            case TYPE2 -> BigDecimal.valueOf(0.10);
            case TYPE3 -> BigDecimal.valueOf(0.15);
        };
        return BigDecimal.valueOf(hours).multiply(coefficient).setScale(2, RoundingMode.HALF_UP);
    }

    public List<Payment> getFilteredPayments(BigDecimal amountMin, BigDecimal amountMax) {
        paymentValidator.validateFilteredPaymentsQuery(amountMin, amountMax);
        return repo.findActivePaymentsWithOptionalMinAmount(amountMin, amountMax);
    }
}
