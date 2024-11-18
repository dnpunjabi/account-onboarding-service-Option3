package com.bank.onboarding.util;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestClientUtil {

    private final RestTemplate restTemplate;

    public RestClientUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> makePostCall(String url, Object requestPayload) {
        return restTemplate.postForEntity(url, requestPayload, String.class);
    }

    public ResponseEntity<String> makeGetCall(String url) {
        return restTemplate.getForEntity(url, String.class);
    }

    // You can add more methods for PUT, DELETE, etc.
}