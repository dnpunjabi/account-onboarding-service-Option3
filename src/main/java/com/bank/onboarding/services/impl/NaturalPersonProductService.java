package com.bank.onboarding.services.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bank.onboarding.config.ProductConfig;
import com.bank.onboarding.features.factory.ProductFeatureFactory;
import com.bank.onboarding.services.OrinocoCaseManagementService;
import com.bank.onboarding.services.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NaturalPersonProductService implements ProductService {

    private final ProductFeatureFactory featureFactory;
    private final ProductConfig productConfig;
    private final OrinocoCaseManagementService caseManagementService;

    @Autowired
    public NaturalPersonProductService(ProductFeatureFactory featureFactory, ProductConfig productConfig, OrinocoCaseManagementService caseManagementService) {
        this.featureFactory = featureFactory;
        this.productConfig = productConfig;
        this.caseManagementService = caseManagementService;
    }

    @Override
    public String process(Map<String, Object> requestContext) {
        String productCode = (String) requestContext.get("productCode");

        ProductConfig.Product product = findProductByCode(productCode);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productCode);
        }

        try {

            // Perform SCHUFA Check for Natural Person
            if (product.getFeatures().getSchufa()) {
                featureFactory.getFeature("schufa-check").execute(requestContext);
            }

            // Onboarding
            if (product.getFeatures().getAccountOpening()) {
                featureFactory.getFeature("account-opening").execute(requestContext);
            }

            // PIN Activation
            boolean pinSet = (boolean) requestContext.getOrDefault("pinSet", false);
            if (product.getFeatures().getPinActivation() && pinSet) {
                featureFactory.getFeature("activate-pin").execute(requestContext);
            } else if (product.getFeatures().getPinActivation() && !pinSet) {
                log.info("Skipping PIN activation as the customer did not set a PIN.");
            }

            // Online Banking Activation
            boolean onlineBankingRequested = (boolean) requestContext.getOrDefault("onlineBankingOptIn", false); // Assuming a flag in the request context
            if (product.getFeatures().getOnlineBankingActivation() && onlineBankingRequested) {
                featureFactory.getFeature("activate-online-banking").execute(requestContext);
                } else if (product.getFeatures().getOnlineBankingActivation() && !onlineBankingRequested) {
                log.info("Skipping online banking activation as the customer did not request it.");
            }


        } catch (Exception ex) {
            return handleFailure(requestContext, ex.getMessage());
        }

        return null;
    }

    private ProductConfig.Product findProductByCode(String productCode) {
        return productConfig.getItems().stream()
                .filter(product -> product.getProductCode().equalsIgnoreCase(productCode))
                .findFirst()
                .orElse(null);
    }

    private String handleFailure(Map<String, Object> requestContext, String errorDetails) {
        // Pass the entire request context and error message
        return caseManagementService.notifyBackOffice(requestContext, errorDetails);
    }
}