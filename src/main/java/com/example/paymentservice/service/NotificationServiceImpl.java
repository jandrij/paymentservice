package com.example.paymentservice.service;

import com.example.paymentservice.entity.NotificationLog;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Value("${notification.type1.url}")
    private String type1NotificationUrl;

    @Value("${notification.type2.url}")
    private String type2NotificationUrl;

    private final RestTemplate restTemplate;
    private final NotificationLogRepository repo;

    @Async
    @Override
    public void notifyExternalService(Payment payment) {
        String url = switch (payment.getType()) {
            case TYPE1 -> type1NotificationUrl;
            case TYPE2 -> type2NotificationUrl;
            default -> null;
        };

        if (url == null) {
            return;
        }

        boolean isSuccessful = true;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        } catch (RestClientException e) {
            isSuccessful = false;
        }
        saveNotificationLog(payment.getId(), url, isSuccessful);
    }

    private void saveNotificationLog(Long paymentId, String url, boolean isSuccessful) {
        repo.save(NotificationLog.builder()
                .paymentId(paymentId)
                .url(url)
                .success(isSuccessful)
                .build());
    }

}
