package com.bank.onboarding.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "products")
@Data
public class ProductConfig {
    private List<Product> items;

    @Data
    public static class Product {
        private String name;
        private String productCode;
        private Features features;
    }

    @Data
    public static class Features {
        private Boolean schufa;
        private Boolean accountOpening;
        private Boolean pinActivation;
        private Boolean onlineBankingActivation;
    }
}
