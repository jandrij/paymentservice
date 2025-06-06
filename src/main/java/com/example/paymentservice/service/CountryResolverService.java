package com.example.paymentservice.service;

import org.springframework.scheduling.annotation.Async;

public interface CountryResolverService {

    void resolveAndLog(String ip);
}
