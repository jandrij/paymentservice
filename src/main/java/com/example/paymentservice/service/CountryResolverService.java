package com.example.paymentservice.service;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CountryResolverService {

    private static final Logger log = LoggerFactory.getLogger(CountryResolverService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void resolveAndLog(String ip) {
        try {
            String url = "https://ipapi.co/" + ip + "/country_name/";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String country = response.getBody();
                log.info("Client from country: {}", country);
            } else {
                log.warn("Failed to resolve country for IP: {}", ip);
            }
        } catch (Exception e) {
            log.warn("Error resolving country for IP: {}", ip, e);
        }
    }
}
