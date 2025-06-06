package com.example.paymentservice.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class CountryResolverServiceImpl implements CountryResolverService {

    private static final Logger log = LoggerFactory.getLogger(CountryResolverServiceImpl.class);

    private final RestTemplate restTemplate;

    @Async
    @Override
    public void resolveAndLog(String ip) {
        try {
            String url = "https://ipapi.co/" + ip + "/country_name/";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String country = response.getBody();
            if (response.getStatusCode().is2xxSuccessful()
                    && StringUtils.isNotBlank(country)
                    && !country.equals("Undefined")) {

                log.info("Client from country: {}", country);
            } else {
                log.warn("Failed to resolve country for IP: {}", ip);
            }
        } catch (Exception e) {
            log.warn("Error resolving country for IP: {}", ip, e);
        }
    }
}
