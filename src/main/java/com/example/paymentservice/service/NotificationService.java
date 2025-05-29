package com.example.paymentservice.service;

import com.example.paymentservice.entity.NotificationLog;
import com.example.paymentservice.entity.Payment;
import com.example.paymentservice.repository.NotificationLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationService {

    public static final String TYPE1_NOTIFICATION_URL = "https://httpbin.org/status/200";
    public static final String TYPE2_NOTIFICATION_URL = "https://httpbin.org/status/500";

    private final RestTemplate restTemplate = new RestTemplate();

    private final NotificationLogRepository repo;

    public NotificationService(NotificationLogRepository repo) {
        this.repo = repo;
    }

    @Async
    public void notifyExternalService(Payment payment) {
        String url = switch (payment.getType()) {
            case TYPE1 -> TYPE1_NOTIFICATION_URL;
            case TYPE2 -> TYPE2_NOTIFICATION_URL;
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
