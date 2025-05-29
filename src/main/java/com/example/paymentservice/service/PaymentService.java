package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.types.CurrencyType;
import com.example.paymentservice.exception.BusinessValidationException;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository repo;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository repo, NotificationService notificationService) {
        this.repo = repo;
        this.notificationService = notificationService;
    }

    @Async
    public void notifyAndSaveStatus(Payment payment) {
        Boolean success = notificationService.notifyExternalService(payment);
        if (success == null) {
            return;
        }
        payment.setNotificationSuccess(success);
        repo.save(payment);
    }

    public Long createPayment(Payment payment) {
        validatePayment(payment);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setIsCanceled(Boolean.FALSE);
        Payment savedPayment = repo.save(payment);
        notifyAndSaveStatus(savedPayment);
        return savedPayment.getId();
    }

    public Optional<Payment> getPayment(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public BigDecimal cancelPayment(Long id) {
        try {
            Payment payment = repo.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
            if (Boolean.TRUE.equals(payment.getIsCanceled())){
                throw new BusinessValidationException("Payment is already canceled");
            }
            if (!LocalDateTime.now().toLocalDate().equals(payment.getCreatedAt().toLocalDate())) {
                throw new BusinessValidationException("Payment can only be cancel on the same day");
            }
            payment.setIsCanceled(Boolean.TRUE);
            BigDecimal fee = calculateCancellationFee(payment);
            payment.setCancellationFee(fee);
            repo.save(payment);
            return fee;
        } catch (OptimisticLockException ex) {
            throw new RuntimeException("Payment was modified concurrently. Please retry.");
        }
    }

    public BigDecimal calculateCancellationFee(Payment payment) {
        long hours = Duration.between(payment.getCreatedAt(), LocalDateTime.now()).toHours();
        BigDecimal coefficient = switch (payment.getType()) {
            case TYPE1 -> BigDecimal.valueOf(0.05);
            case TYPE2 -> BigDecimal.valueOf(0.10);
            case TYPE3 -> BigDecimal.valueOf(0.15);
        };
        return BigDecimal.valueOf(hours).multiply(coefficient);
    }

    private void validatePayment(Payment payment) {
        switch (payment.getType()) {
            case TYPE1 -> {
                if (!CurrencyType.EUR.equals(payment.getCurrency())) {
                    throw new BusinessValidationException("Payment of TYPE1 must be EUR");
                }
                if (payment.getDetails() == null || payment.getDetails().isEmpty()) {
                    throw new BusinessValidationException("Details are required for TYPE1 payment");
                }
            }
            case TYPE2 -> {
                if (!CurrencyType.USD.equals(payment.getCurrency())) {
                    throw new BusinessValidationException("Payment of TYPE2 must be USD");
                }
            }
            case TYPE3 -> {
                if (StringUtils.isBlank(payment.getCreditorBankBic())) {
                    throw new BusinessValidationException("Creditor bank BIC is required for TYPE3 payment");
                }
            }
        }
    }

    public List<Payment> getFilteredPayments(BigDecimal amountMin) {
        if (amountMin != null && amountMin.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessValidationException("Monetary value can not be negative");
        }
        return repo.findActivePaymentsWithOptionalMinAmount(amountMin);
    }
}
