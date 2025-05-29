package com.example.paymentservice.dto;

import com.example.paymentservice.types.CurrencyType;
import com.example.paymentservice.types.PaymentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequestDto {
    @Min(value = 0, message = "Monetary value can not be negative")
    @NotNull
    private BigDecimal amount;
    @NotNull
    private CurrencyType currency;
    @NotNull
    private String debtorIban;
    @NotNull
    private String creditorIban;
    @NotNull
    private PaymentType type;
    private String details;
    private String creditorBankBic;
}
