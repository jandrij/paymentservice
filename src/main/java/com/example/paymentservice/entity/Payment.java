package com.example.paymentservice.entity;

import com.example.paymentservice.types.CurrencyType;
import com.example.paymentservice.types.PaymentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "PAYMENTS")
public class Payment {
    @Id
    @GeneratedValue
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "AMOUNT", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "CURRENCY", nullable = false)
    private CurrencyType currency;

    @Column(name = "DEBTOR_IBAN", nullable = false)
    private String debtorIban;

    @Column(name = "CREDITOR_IBAN", nullable = false)
    private String creditorIban;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private PaymentType type;

    @Column(name = "DETAILS")
    private String details;

    @Column(name = "CREDITOR_BANK_BIC")
    private String creditorBankBic;

    @Column(name = "IS_CANCELED", nullable = false)
    private Boolean isCanceled;

    @Column(name = "CANCELLATION_FEE")
    private BigDecimal cancellationFee;

    @Column(name = "NOTIFICATION_SUCCESS")
    private Boolean notificationSuccess;

    @Version
    @Column(name = "VERSION")
    private Long version;
}
