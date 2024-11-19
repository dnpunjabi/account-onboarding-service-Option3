package com.bank.onboarding.util;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RestClientUtilDummy {

    private final RestTemplate restTemplate; // Placeholder for actual RestTemplate calls

    public RestClientUtilDummy(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Simulates a POST request to an upstream API.
     * If the request payload contains 'simulateFailure': true, it simulates a failure.
     */
    public ResponseEntity<String> makePostCall(String url, Map<String, Object> requestPayload) {
        log.info("Simulating POST request to URL: {} with payload: {}", url, requestPayload);

        // Simulate failure based on the 'simulateFailure' flag in the payload
        if (requestPayload != null && requestPayload.containsKey("simulateFailure") && (Boolean) requestPayload.get("simulateFailure")) {
            log.error("Simulating API failure for testing");
            return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Simulated API failure\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Simulated Success Responses
        if (url.contains("onboard")) {
            log.info("Simulating successful onboarding response");
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Onboarding successful\"}", HttpStatus.OK);
        } else if (url.contains("activate-pin")) {
            log.info("Simulating successful PIN activation response");
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"PIN activated\"}", HttpStatus.OK);
        } else if (url.contains("activate-online-banking")) {
            log.info("Simulating successful online banking activation response");
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Online banking activated\"}", HttpStatus.OK);
        }

        // Default error response for unknown URLs
        return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Unknown endpoint\"}", HttpStatus.NOT_FOUND);
    }

    /**
     * Simulates a POST request to Orinoco Case Management API.
     * 
     * @param url The URL to call.
     * @param casePayload The payload containing case details.
     * @return A ResponseEntity simulating the Orinoco API response (success or failure).
     */
    public ResponseEntity<String> makeCaseManagementCall(String url, Object casePayload) {
        log.info("Simulating Orinoco Case Management POST request to URL: {} with payload: {}", url, casePayload);

        // Simulate success or failure based on casePayload (for testing, we can add a failure flag in the payload)
        if (url.contains("case-management")) {
            // Simulate a successful case creation response
            log.info("Simulating successful Orinoco Case Management response");
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Case management notified successfully\"}", HttpStatus.OK);
        }

        // Simulate failure for testing purposes
        if (url.contains("error-trigger")) {
            log.error("Simulating Orinoco Case Management API failure");
            return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Simulated Orinoco Case Management API failure\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Default response for unknown URLs
        return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Unknown endpoint\"}", HttpStatus.NOT_FOUND);
    }

    /**
     * Simulates a GET request to an upstream API.
     */
    public ResponseEntity<String> makeGetCall(String url) {
        log.info("Simulating GET request to URL: {}", url);

        if (url.contains("check-status")) {
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Status checked\"}", HttpStatus.OK);
        }

        return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Unknown endpoint\"}", HttpStatus.NOT_FOUND); // Default for unknown URLs
    }
}