package com.bank.onboarding.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bank.onboarding.config.ProductConfig;
import com.bank.onboarding.dto.OrinocoCasePayload;
import com.bank.onboarding.util.RestClientUtilDummy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

    private final RestClientUtilDummy restClient;
    private final ProductConfig productConfig;
    private final OrinocoCaseManagementService caseManagementService;

    // Define base URLs for different features
    private static final Map<String, String> API_URLS = new HashMap<>();

    static {
        API_URLS.put("onboarding", "https://upstream.api/onboard");
        API_URLS.put("activate-pin", "https://upstream.api/activate-pin");
        API_URLS.put("activate-online-banking", "https://upstream.api/activate-online-banking");
    }

    @Autowired
    public ProductService(RestClientUtilDummy restClient, ProductConfig productConfig, OrinocoCaseManagementService caseManagementService) {
        this.restClient = restClient;
        this.productConfig = productConfig;
        this.caseManagementService = caseManagementService;
    }

    public String onboardProduct(String fkn, String productCode, boolean simulateFailure) {
        ProductConfig.Product product = findProductByCode(productCode);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productCode);
        }

        try {
            // Onboarding
            if (product.getFeatures().getOnboarding()) {
                callUpstreamApi("onboarding", fkn, productCode, buildOnboardingPayload(fkn, simulateFailure));
            }

            // PIN Activation
            if (product.getFeatures().getPinActivation()) {
                callUpstreamApi("activate-pin", fkn, productCode, buildPinActivationPayload(fkn, simulateFailure));
            }

            // Online Banking Activation
            if (product.getFeatures().getOnlineBankingActivation()) {
                callUpstreamApi("activate-online-banking", fkn, productCode, buildOnlineBankingPayload(fkn, simulateFailure));
            }

        } catch (Exception ex) {
            log.error("Exception occurred during processing for FKN: {}, ProductCode: {}. Stopping further processing.", fkn, productCode);

            // Trigger Orinoco case management and return case ID
            return handleFailure(fkn, productCode, ex.getMessage());
        }

        log.info("Product onboarding complete for {}", product.getName());
        return null;  // No case ID returned if everything succeeds
    }

    private ProductConfig.Product findProductByCode(String productCode) {
        return productConfig.getItems().stream()
                .filter(product -> product.getProductCode().equalsIgnoreCase(productCode))
                .findFirst()
                .orElse(null);
    }

    private void callUpstreamApi(String action, String fkn, String productCode, Map<String, Object> payload) {
        String url = API_URLS.get(action) + "?fkn=" + fkn + "&productCode=" + productCode;
        log.info("Calling upstream API for action: {}, URL: {}", action, url);

        ResponseEntity<String> response = restClient.makePostCall(url, payload);
        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Upstream API returned non-success status: {}", response.getStatusCode());
            throw new RuntimeException("Upstream API failed: " + action + " | Status: " + response.getStatusCode());
        }
    }

    // Handle failure scenario when the upstream API fails
    private String handleFailure(String fkn, String productCode, String errorDetails) {
        log.error("Failed to complete onboarding. Triggering Orinoco Case Management for FKN: {}, ProductCode: {}. Error: {}", fkn, productCode, errorDetails);

        // Prepare payload for the Orinoco Case Management fallback
        OrinocoCasePayload casePayload = new OrinocoCasePayload();
        casePayload.setFkn(fkn);
        casePayload.setProductCode(productCode);
        casePayload.setFailureMessage("Failed processing for ProductCode: " + productCode + " | Error: " + errorDetails);

        // Notify Orinoco Case Management system and get back a case ID
        String caseId = caseManagementService.notifyBackOffice(casePayload);
        return caseId;  // Return the case ID to the client
    }

    // Simulate payload for onboarding with failure flag
    private Map<String, Object> buildOnboardingPayload(String fkn, boolean simulateFailure) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fkn", fkn);
        payload.put("action", "onboard");
        payload.put("simulateFailure", simulateFailure);  // Add simulate failure flag
        return payload;
    }

    // Simulate payload for PIN activation
    private Map<String, Object> buildPinActivationPayload(String fkn, boolean simulateFailure) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fkn", fkn);
        payload.put("action", "activate-pin");
        payload.put("simulateFailure", simulateFailure);  // Add simulate failure flag
        return payload;
    }

    // Simulate payload for online banking activation
    private Map<String, Object> buildOnlineBankingPayload(String fkn, boolean simulateFailure) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("fkn", fkn);
        payload.put("action", "activate-online-banking");
        payload.put("simulateFailure", simulateFailure);  // Add simulate failure flag
        return payload;
    }
}