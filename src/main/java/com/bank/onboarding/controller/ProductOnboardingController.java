package com.bank.onboarding.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onboarding.config.ProductConfig;
import com.bank.onboarding.dto.OnboardingRequest;
import com.bank.onboarding.service.ProductService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductOnboardingController {

    private final ProductService productService;
    private final ProductConfig productConfig;
    
    @Autowired
    public ProductOnboardingController(ProductService productService, ProductConfig productConfig) {
    this.productService = productService;
    this.productConfig = productConfig;
    }

    @PostMapping("/onboard")
    public ResponseEntity<String> onboardProduct(@RequestBody OnboardingRequest request) {
        log.info("Received Onboarding Request: {}", request);
        
        // Attempt to onboard the product, return a case ID if case management is triggered
        String caseId = productService.onboardProduct(request.getFkn(), request.getProductCode(), request.isSimulateFailure());
        
        if (caseId != null) {
            // Bank will contact the client
            String message = "An issue occurred. Case ID: " + caseId + ". The bank will contact you for further processing.";
            return ResponseEntity.ok(message);
        }

        return ResponseEntity.ok("Onboarding for product " + request.getProductCode() + " linked to FKN " + request.getFkn() + " completed successfully.");
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