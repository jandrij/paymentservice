package com.example.paymentservice.entity;

import com.example.paymentservice.types.CurrencyType;
import com.example.paymentservice.types.PaymentType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue
    private Long id;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private CurrencyType currency;
    private String debtorIban;
    private String creditorIban;
    @Enumerated(EnumType.STRING)
    private PaymentType type;
    private String details;
    private String creditorBankBic;
    private Boolean isCanceled;
    private LocalDateTime createdAt;
    private BigDecimal cancellationFee;
    private Boolean notificationSuccess;
    @Version
    private Long version;
}
