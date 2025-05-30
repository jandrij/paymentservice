package com.example.paymentservice.dto;

import com.example.paymentservice.types.CurrencyType;
import com.example.paymentservice.types.PaymentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequestDto {
    @Min(value = 0, message = "Monetary value can not be negative")
    @NotNull(message = "Amount is a required field")
    private BigDecimal amount;

    @NotNull(message = "Currency is a required field")
    private CurrencyType currency;

    @NotNull(message = "Debtor IBAN is a required field")
    private String debtorIban;

    @NotNull(message = "Creditor IBAN is a required field")
    private String creditorIban;

    @NotNull(message = "Payment type is a required field")
    private PaymentType type;

    private String details;

    private String creditorBankBic;
}
