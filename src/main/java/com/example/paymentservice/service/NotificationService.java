package com.example.paymentservice.service;

import com.example.paymentservice.entity.Payment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationService {

    public static final String TYPE1_NOTIFICATION_URL = "https://example.com/type1Notify";
    public static final String TYPE2_NOTIFICATION_URL = "https://example.com/type2Notify";

    private final RestTemplate restTemplate = new RestTemplate();

    public Boolean notifyExternalService(Payment payment) {
        String url = switch (payment.getType()) {
            case TYPE1 -> TYPE1_NOTIFICATION_URL;
            case TYPE2 -> TYPE2_NOTIFICATION_URL;
            default -> null;
        };

        if (url == null) {
            return null; // No notification for TYPE3 or others
        }

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (RestClientException e) {
            return Boolean.FALSE;
        }
    }
}
