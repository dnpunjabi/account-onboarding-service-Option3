package com.bank.onboarding.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onboarding.audit.ApiCallLogService;
import com.bank.onboarding.config.ProductConfig;
import com.bank.onboarding.dto.OnboardingRequest;
import com.bank.onboarding.enums.CustomerType;
import com.bank.onboarding.services.ProductService;
import com.bank.onboarding.services.factory.ProductServiceFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductOnboardingController {

    private final ProductServiceFactory productServiceFactory;
    private final ProductConfig productConfig;
    private final ApiCallLogService apiCallLogService;

    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON serialization

    @Autowired
    public ProductOnboardingController(ProductServiceFactory productServiceFactory, 
                                       ProductConfig productConfig, 
                                       ApiCallLogService apiCallLogService) {
        this.productServiceFactory = productServiceFactory;
        this.productConfig = productConfig;
        this.apiCallLogService = apiCallLogService;
    }

    @PostMapping("/onboard")
public ResponseEntity<String> onboardProduct(@RequestBody OnboardingRequest request) {
    log.info("Received onboarding request: {}", request);

    String transactionId = request.getTransactionId();
    String fkn = request.getFkn();
    String productCode = request.getProductCode();

    CustomerType customerType = CustomerType.fromCode(request.getCustomerType());

    Map<String, Object> requestContext = new HashMap<>();
    requestContext.put("transactionId", transactionId);
    requestContext.put("fkn", fkn);
    requestContext.put("productCode", productCode);
    requestContext.put("simulateFailure", request.getSimulateFailure());
    requestContext.put("failureTarget", request.getFailureTarget());
    requestContext.put("pinSet", request.isPinSet());
    requestContext.put("onlineBankingOptIn", request.isOnlineBankingOptIn());
    requestContext.put("customerType", customerType);

    log.info("Request Context: {}", requestContext);

    String requestPayloadJson = "";
    String finalResponseMessage;

    try {
        // Serialize request payload to JSON
        requestPayloadJson = objectMapper.writeValueAsString(request);

        // Get the appropriate service based on the customer type
        ProductService productService = productServiceFactory.getService(customerType);
        String caseId = productService.process(requestContext);

        if (caseId != null) {
            finalResponseMessage = "An issue occurred. Case ID: " + caseId +
                                   ". The bank will contact you for further processing.";
        } else {
            finalResponseMessage = "Onboarding for product " + productCode +
                                   " Transaction Id: " + transactionId +
                                   " linked to FKN " + fkn +
                                   " completed successfully.";
        }

        // Log the final response in the database
        apiCallLogService.logApiResponse(
            transactionId,
            "onboard-product",
            fkn,
            productCode,
            "200 OK",
            requestPayloadJson, // Valid JSON for request payload
            objectMapper.writeValueAsString(finalResponseMessage) // Serialize final response as JSON
        );

        return ResponseEntity.ok(finalResponseMessage);

    } catch (JsonProcessingException e) {
        finalResponseMessage = "Onboarding failed for product " + productCode +
                               " Transaction Id: " + transactionId +
                               " with error: " + e.getMessage();

        log.error("Error during onboarding: {}", e.getMessage(), e);

        try {
            // Log the failure response in the database
            apiCallLogService.logApiResponse(
                transactionId,
                "onboard-product",
                fkn,
                productCode,
                "500 INTERNAL_SERVER_ERROR",
                requestPayloadJson, // Valid JSON for request payload
                objectMapper.writeValueAsString(finalResponseMessage) // Serialize error response as JSON
            );
        } catch (JsonProcessingException logException) {
            log.error("Error logging to database: {}", logException.getMessage(), logException);
        }

        return ResponseEntity.status(500).body(finalResponseMessage);
    }
    }

    @GetMapping("/config")
    public ResponseEntity<List<ProductConfig.Product>> getProductsConfig() {
        List<ProductConfig.Product> products = productConfig.getItems();
        log.info("Retrieving Products Config. Total Products: {}", products.size());
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(products);
    }
}