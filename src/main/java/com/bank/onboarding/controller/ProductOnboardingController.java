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

import com.bank.onboarding.config.ProductConfig;
import com.bank.onboarding.dto.OnboardingRequest;
import com.bank.onboarding.enums.CustomerType;
import com.bank.onboarding.services.ProductService;
import com.bank.onboarding.services.factory.ProductServiceFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductOnboardingController {

    private final ProductServiceFactory productServiceFactory;
    private final ProductConfig productConfig;

    @Autowired
    public ProductOnboardingController(ProductServiceFactory productServiceFactory, ProductConfig productConfig) {
    this.productServiceFactory = productServiceFactory;
    this.productConfig = productConfig;
    }
    @PostMapping("/onboard")
    public ResponseEntity<String> onboardProduct(@RequestBody OnboardingRequest request) {
        log.info("Received onboarding request: {}", request); // Log the request
        // Convert the numeric customerType to a CustomerType enum
        CustomerType customerType = CustomerType.fromCode(request.getCustomerType());

        // Build the request context from the onboarding request
        Map<String, Object> requestContext = new HashMap<>();
        requestContext.put("transactionId", request.getTransactionId());
        requestContext.put("fkn", request.getFkn());
        requestContext.put("productCode", request.getProductCode());
        requestContext.put("simulateFailure", request.getSimulateFailure());  // Pass as String
        requestContext.put("failureTarget", request.getFailureTarget());      // Target for failure
        requestContext.put("pinSet", request.isPinSet());
        requestContext.put("onlineBankingOptIn", request.isOnlineBankingOptIn());
        requestContext.put("customerType", customerType);  // Store as CustomerType enum

        log.info("Request Context: {}", requestContext);  // Log the request context
        // Get the appropriate service based on the customer type
        ProductService productService = productServiceFactory.getService(customerType);

        String caseId = productService.process(requestContext);

        // Handle the response based on whether a case ID was returned
        if (caseId != null) {
            return ResponseEntity.ok("An issue occurred. Case ID: " + caseId + ". The bank will contact you for further processing.");
        }

        return ResponseEntity.ok("Onboarding for product " + request.getProductCode() +
            " Transaction Id: " + request.getTransactionId() +
            " linked to FKN " + request.getFkn() +
            " completed successfully.");
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