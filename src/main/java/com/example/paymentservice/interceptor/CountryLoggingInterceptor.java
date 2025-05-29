package com.example.paymentservice.interceptor;

import com.example.paymentservice.service.CountryResolverService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CountryLoggingInterceptor implements HandlerInterceptor {

    private final CountryResolverService countryResolverService;

    public CountryLoggingInterceptor(CountryResolverService countryResolverService) {
        this.countryResolverService = countryResolverService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = resolveClientIp(request);
        countryResolverService.resolveAndLog(ip);
        return true;
    }

//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//
//        String ip = resolveClientIp(request);
//
//        // Optional: skip localhost or internal IPs
//        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
//            log.debug("Skipping country lookup for local IP: {}", ip);
//            return true;
//        }
//
//        try {
//            String url = "https://ipapi.co/" + ip + "/country_name/";
//            ResponseEntity<String> countryResponse = restTemplate.getForEntity(url, String.class);
//
//            if (countryResponse.getStatusCode().is2xxSuccessful()) {
//                String country = countryResponse.getBody();
//                log.info("Request from IP [{}], resolved country: {}", ip, country);
//            } else {
//                log.warn("Failed to resolve country for IP [{}], status: {}", ip, countryResponse.getStatusCode());
//            }
//        } catch (Exception e) {
//            log.warn("Could not resolve country for IP [{}]: {}", ip, e.getMessage());
//        }
//
//        return true; // let the request proceed
//    }
//
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}