package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.PaymentRepository;
import com.example.paymentservice.types.CurrencyType;
import com.example.paymentservice.exception.BusinessValidationException;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository repo;
    private final NotificationService notificationService;

    public PaymentService(PaymentRepository repo, NotificationService notificationService) {
        this.repo = repo;
        this.notificationService = notificationService;
    }

    public Long createPayment(Payment payment) {
        validatePayment(payment);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setIsCanceled(Boolean.FALSE);
        Payment savedPayment = repo.save(payment);
        notificationService.notifyExternalService(savedPayment);
        return savedPayment.getId();
    }

    public Payment getPayment(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Transactional
    public Payment cancelPayment(Long id) {
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
            return repo.save(payment);
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
        return BigDecimal.valueOf(hours).multiply(coefficient).setScale(2, RoundingMode.HALF_UP);
    }

    private void validatePayment(Payment payment) {
        if (payment.getAmount().scale() != 2) {
            throw new BusinessValidationException("Monetary amount must have exactly 2 decimal places");
        }
        switch (payment.getType()) {
            case TYPE1 -> {
                if (!CurrencyType.EUR.equals(payment.getCurrency())) {
                    throw new BusinessValidationException("Payment of TYPE1 must be EUR");
                }
                if (payment.getDetails() == null || payment.getDetails().isEmpty()) {
                    throw new BusinessValidationException("Details are required for TYPE1 payment");
                }
                if (payment.getCreditorBankBic() != null) {
                    throw new BusinessValidationException("Creditor Bank BIC is not allowed for TYPE1 payments");
                }
            }
            case TYPE2 -> {
                if (!CurrencyType.USD.equals(payment.getCurrency())) {
                    throw new BusinessValidationException("Payment of TYPE2 must be USD");
                }
                if (payment.getCreditorBankBic() != null) {
                    throw new BusinessValidationException("Creditor Bank BIC is not allowed for TYPE2 payments");
                }
            }
            case TYPE3 -> {
                if (StringUtils.isBlank(payment.getCreditorBankBic())) {
                    throw new BusinessValidationException("Creditor bank BIC is required for TYPE3 payment");
                }
                if (payment.getDetails() != null) {
                    throw new BusinessValidationException("Details are not allowed for TYPE3 payments");
                }
            }
        }
    }

    public List<Payment> getFilteredPayments(BigDecimal amountMin, BigDecimal amountMax) {
        if ((amountMin != null && amountMin.compareTo(BigDecimal.ZERO) < 0)
                || (amountMax != null && amountMax.compareTo(BigDecimal.ZERO) < 0)) {
            throw new BusinessValidationException("Monetary value can not be negative");
        }
        if (amountMin != null && amountMax != null && amountMax.compareTo(amountMin) < 0) {
            throw new BusinessValidationException("AmountMax should be larger then or equal to AmountMin");
        }
        return repo.findActivePaymentsWithOptionalMinAmount(amountMin, amountMax);
    }
}
