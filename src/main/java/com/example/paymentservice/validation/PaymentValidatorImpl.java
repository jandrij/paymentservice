package com.example.paymentservice.validation;

import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.exception.BusinessValidationException;
import com.example.paymentservice.types.CurrencyType;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentValidatorImpl implements PaymentValidator {

    private final Clock clock;

    @Override
    public void validateNewPayment(Payment payment) {
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

    @Override
    public void validateFilteredPaymentsQuery(BigDecimal amountMin, BigDecimal amountMax) {
        if ((amountMin != null && amountMin.compareTo(BigDecimal.ZERO) < 0)
                || (amountMax != null && amountMax.compareTo(BigDecimal.ZERO) < 0)) {
            throw new BusinessValidationException("Monetary value can not be negative");
        }
        if (amountMin != null && amountMax != null && amountMax.compareTo(amountMin) < 0) {
            throw new BusinessValidationException("AmountMax should be larger then or equal to AmountMin");
        }
    }

    @Override
    public void validatePaymentToBeCancelled(Payment payment) {
        if (Boolean.TRUE.equals(payment.getIsCanceled())){
            throw new BusinessValidationException("Payment is already canceled");
        }
        if (!LocalDateTime.now(clock).toLocalDate().equals(payment.getCreatedAt().toLocalDate())) {
            throw new BusinessValidationException("Payment can only be cancel on the same day");
        }
    }
}
