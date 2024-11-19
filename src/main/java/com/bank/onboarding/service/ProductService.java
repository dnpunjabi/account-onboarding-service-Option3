package com.bank.onboarding.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bank.onboarding.config.ProductConfig;
import com.bank.onboarding.dto.OrinocoCasePayload;
import com.bank.onboarding.features.ProductFeatureFactory;
import com.bank.onboarding.util.RestClientUtilDummy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

    private final RestClientUtilDummy restClient;
    private final ProductConfig productConfig;
    private final OrinocoCaseManagementService caseManagementService;
    private final ProductFeatureFactory featureFactory;

    @Autowired
    public ProductService(RestClientUtilDummy restClient, ProductConfig productConfig, 
                          OrinocoCaseManagementService caseManagementService, ProductFeatureFactory featureFactory) {
        this.restClient = restClient;
        this.productConfig = productConfig;
        this.caseManagementService = caseManagementService;
        this.featureFactory = featureFactory;
    }

    public String onboardProduct(String fkn, String productCode, boolean simulateFailure) {
        ProductConfig.Product product = findProductByCode(productCode);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productCode);
        }

        try {
            // Onboarding
            if (product.getFeatures().getOnboarding()) {
                featureFactory.getFeature("onboarding").execute(fkn, productCode, simulateFailure);
            }

            // PIN Activation
            if (product.getFeatures().getPinActivation()) {
                featureFactory.getFeature("activate-pin").execute(fkn, productCode, simulateFailure);
            }

            // Online Banking Activation
            if (product.getFeatures().getOnlineBankingActivation()) {
                featureFactory.getFeature("activate-online-banking").execute(fkn, productCode, simulateFailure);
            }

        } catch (Exception ex) {
            log.error("Exception occurred during onboarding for FKN: {}, ProductCode: {}. Stopping further processing.", fkn, productCode);
            return handleFailure(fkn, productCode, ex.getMessage());
        }

        log.info("Product onboarding complete for {}", product.getName());
        return null;
    }

    private ProductConfig.Product findProductByCode(String productCode) {
        return productConfig.getItems().stream()
                .filter(product -> product.getProductCode().equalsIgnoreCase(productCode))
                .findFirst()
                .orElse(null);
    }

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
}