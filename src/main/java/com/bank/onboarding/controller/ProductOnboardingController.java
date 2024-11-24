package com.bank.onboarding.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onboarding.audit.ApiCallLogService;
import com.bank.onboarding.dto.OnboardingRequest;
import com.bank.onboarding.enums.CustomerType;
import com.bank.onboarding.enums.ProductType;
import com.bank.onboarding.services.CustomerProductService;
import com.bank.onboarding.services.factory.CustomerProductServiceFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductOnboardingController {

    private final CustomerProductServiceFactory customerProductServiceFactory;
    private final ApiCallLogService apiCallLogService;

    private final ObjectMapper objectMapper = new ObjectMapper(); // For JSON serialization

    @Autowired
    public ProductOnboardingController(CustomerProductServiceFactory customerProductServiceFactory,
                                       ApiCallLogService apiCallLogService) {
        this.customerProductServiceFactory = customerProductServiceFactory;
        this.apiCallLogService = apiCallLogService;
    }

    @PostMapping("/onboard")
    public ResponseEntity<String> onboardProduct(@RequestBody OnboardingRequest request) {
        log.info("Received onboarding request: {}", request);

        String transactionId = request.getTransactionId();
        String fkn = request.getFkn();
        String productCode = request.getProductCode();

        // Determine customer type and product type
        CustomerType customerType = CustomerType.fromCode(request.getCustomerType());
        ProductType productType = ProductType.fromCode(productCode);

        // Build request context
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

            // Get service from factory and process request
            CustomerProductService service = customerProductServiceFactory.getService(customerType, productType);
            String caseId = service.process(requestContext);

            // Handle response
            if (caseId != null) {
                finalResponseMessage = "An issue occurred. Case ID: " + caseId +
                                       ". The bank will contact you for further processing.";
            } else {
                finalResponseMessage = "Onboarding for product " + productCode +
                                       " Transaction Id: " + transactionId +
                                       " linked to FKN " + fkn +
                                       " completed successfully.";
            }

            // Log the final response
            apiCallLogService.logApiResponse(
                transactionId,
                "onboard-product",
                fkn,
                productCode,
                "200 OK",
                requestPayloadJson,
                objectMapper.writeValueAsString(finalResponseMessage)
            );

            return ResponseEntity.ok(finalResponseMessage);

        } catch (JsonProcessingException e) {
            finalResponseMessage = "Onboarding failed for product " + productCode +
                                   " Transaction Id: " + transactionId +
                                   " with error: " + e.getMessage();

            log.error("Error during onboarding: {}", e.getMessage(), e);

            try {
                // Log the failure response
                apiCallLogService.logApiResponse(
                    transactionId,
                    "onboard-product",
                    fkn,
                    productCode,
                    "500 INTERNAL_SERVER_ERROR",
                    requestPayloadJson,
                    objectMapper.writeValueAsString(finalResponseMessage)
                );
            } catch (JsonProcessingException logException) {
                log.error("Error logging to database: {}", logException.getMessage(), logException);
            }

            return ResponseEntity.status(500).body(finalResponseMessage);
        } catch (Exception e) {
            finalResponseMessage = "Onboarding failed for product " + productCode +
                                   " Transaction Id: " + transactionId +
                                   " with error: " + e.getMessage();

            log.error("Unexpected error during onboarding: {}", e.getMessage(), e);

            try {
                // Log the failure response
                apiCallLogService.logApiResponse(
                    transactionId,
                    "onboard-product",
                    fkn,
                    productCode,
                    "500 INTERNAL_SERVER_ERROR",
                    requestPayloadJson,
                    objectMapper.writeValueAsString(finalResponseMessage)
                );
            } catch (JsonProcessingException logException) {
                log.error("Error logging to database: {}", logException.getMessage(), logException);
            }

            return ResponseEntity.status(500).body(finalResponseMessage);
        }
    }
}