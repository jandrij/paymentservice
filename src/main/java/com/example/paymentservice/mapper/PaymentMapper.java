package com.example.paymentservice.mapper;

import com.example.paymentservice.dto.CreatePaymentRequestDto;
import com.example.paymentservice.dto.PaymentResponseDto;
import com.example.paymentservice.entity.Payment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentMapper {

    public Payment toEntity(CreatePaymentRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }
        return Payment.builder()
                .amount(requestDto.getAmount())
                .currency(requestDto.getCurrency())
                .debtorIban(requestDto.getDebtorIban())
                .creditorIban(requestDto.getCreditorIban())
                .type(requestDto.getType())
                .details(requestDto.getDetails())
                .creditorBankBic(requestDto.getCreditorBankBic())
                .build();
    }

    public PaymentResponseDto toDto(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .cancellationFee(payment.getCancellationFee())
                .build();
    }

    public PaymentResponseDto toDtoIdOnly(Payment payment) {
        if (payment == null) {
            return null;
        }
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .build();
    }

    public List<PaymentResponseDto> toDtoListIdOnly(List<Payment> payments) {
        return payments.stream().map(this::toDtoIdOnly).toList();

    }
}