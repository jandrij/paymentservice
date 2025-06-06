package com.example.paymentservice.service;

import com.example.paymentservice.entity.NotificationLog;
import com.example.paymentservice.entity.Payment;
import org.springframework.scheduling.annotation.Async;

public interface NotificationService {
    void notifyExternalService(Payment payment);
}
