package com.bank.onboarding.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;  // Keep this for when you switch back to real calls

@Component
public class RestClientUtilDummy {

    private final RestTemplate restTemplate; // This will be used later, when not using dummy logic


    public RestClientUtilDummy(RestTemplate restTemplate) { // Keep this constructor for real RestTemplate injection
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> makePostCall(String url, Object requestPayload) {
        if (url.contains("onboard")) {
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Onboarding successful\"}", HttpStatus.OK);
        } else if (url.contains("activate-pin")) {
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"PIN activated\"}", HttpStatus.OK);
        }  else if (url.contains("onlineBankingActivation")) {
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Online banking activated\"}", HttpStatus.OK);
        } // Add more conditions as needed for other upstream endpoints
        else {
          return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Unknown endpoint\"}", HttpStatus.NOT_FOUND); // Default for unknown URLs
        }

    }

    public ResponseEntity<String> makeGetCall(String url) {
        // Similar dummy responses based on the URL
        if (url.contains("check-status")) {
            return new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Status checked\"}", HttpStatus.OK);
        } // Add more conditions as needed
        else {
            return new ResponseEntity<>("{\"status\": \"error\", \"message\": \"Unknown endpoint\"}", HttpStatus.NOT_FOUND); // Default
        }
    }



}
