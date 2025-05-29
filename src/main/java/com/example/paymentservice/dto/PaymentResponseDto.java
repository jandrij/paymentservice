package com.example.paymentservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponseDto {
    private Long id;
    private Boolean isCanceled;
    private BigDecimal cancellationFee;
}