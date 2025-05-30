package com.example.paymentservice.controller;

import com.example.paymentservice.dto.CreatePaymentRequestDto;
import com.example.paymentservice.dto.PaymentResponseDto;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.mapper.PaymentMapper;
import com.example.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService service;
    private final PaymentMapper mapper;

    public PaymentController(PaymentService service, PaymentMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDto> createPayment(@RequestBody @Valid CreatePaymentRequestDto request) {
        Long paymentId = service.createPayment(mapper.toEntity(request));
        URI location = URI.create("/payments/" + paymentId);
        return ResponseEntity.created(location).body(PaymentResponseDto.builder().id(paymentId).build());
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponseDto>> getAllActivePayments(
            @RequestParam(required = false) BigDecimal amountMin,
            @RequestParam(required = false) BigDecimal amountMax) {
        List<Payment> payments = service.getFilteredPayments(amountMin, amountMax);
        return ResponseEntity.ok(mapper.toDtoListIdOnly(payments));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponseDto> cancelPayment(@PathVariable Long id) {
        Payment payment = service.cancelPayment(id);
        return ResponseEntity.ok(mapper.toDto(payment));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable Long id) {
        Payment payment = service.getPayment(id);
        return ResponseEntity.ok(mapper.toDto(payment));
    }
}
