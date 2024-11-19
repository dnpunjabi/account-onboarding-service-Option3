package com.bank.onboarding.features.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bank.onboarding.features.ProductFeature;
import com.bank.onboarding.util.RestClientUtilDummy;

@Service
public class PinActivationFeatureImpl implements ProductFeature {

    private final RestClientUtilDummy restClientUtil;

    @Autowired
    public PinActivationFeatureImpl(RestClientUtilDummy restClientUtil) {
        this.restClientUtil = restClientUtil;
    }

    @Override
    public void execute(String fkn, String productCode, boolean simulateFailure) throws Exception {
        String url = "https://upstream.api/activate-pin?fkn=" + fkn + "&productCode=" + productCode;
        
        // Build the payload based on simulateFailure
        Map<String, Object> payload = new HashMap<>();
        payload.put("fkn", fkn);
        payload.put("productCode", productCode);
        payload.put("simulateFailure", simulateFailure);

        ResponseEntity<String> response = restClientUtil.makePostCall(url, payload);
        
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("PIN Activation failed for productCode: " + productCode);
        }

        System.out.println("PIN Activation completed for " + productCode);
    }
}