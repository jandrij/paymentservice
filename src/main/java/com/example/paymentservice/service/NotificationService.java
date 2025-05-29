package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationService {

    public static final String TYPE1_NOTIFICATION_URL = "https://httpbin.org/status/200";
    public static final String TYPE2_NOTIFICATION_URL = "https://httpbin.org/status/500";

    private final RestTemplate restTemplate = new RestTemplate();

    public Boolean notifyExternalService(Payment payment) {
        String url = switch (payment.getType()) {
            case TYPE1 -> TYPE1_NOTIFICATION_URL;
            case TYPE2 -> TYPE2_NOTIFICATION_URL;
            default -> null;
        };

        if (url == null) {
            return null;
        }

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            return Boolean.FALSE;
        }
    }
}
