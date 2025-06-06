package com.example.paymentservice.interceptor;

import com.example.paymentservice.service.CountryResolverService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class CountryLoggingInterceptor implements HandlerInterceptor {

    private final CountryResolverService countryResolverService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = resolveClientIp(request);
        countryResolverService.resolveAndLog(ip);
        return true;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}