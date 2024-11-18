package com.bank.onboarding.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bank.onboarding.config.ProductConfig;
import com.bank.onboarding.util.RestClientUtilDummy;

@Service
public class ProductService {

    private final RestClientUtilDummy restClient;
    private final ProductConfig productConfig;

    // Define base URLs for different features
    private static final Map<String, String> API_URLS = new HashMap<>();

    static {
        API_URLS.put("onboarding", "https://upstream.api/onboard");
        API_URLS.put("activate-pin", "https://upstream.api/activate-pin");
        API_URLS.put("activate-online-banking", "https://upstream.api/activate-online-banking");
    }

    @Autowired
    public ProductService(RestClientUtilDummy restClient, ProductConfig productConfig) {
        this.restClient = restClient;
        this.productConfig = productConfig;
    }

    public void onboardProduct(String fkn, String productCode) {
        ProductConfig.Product product = findProductByCode(productCode);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productCode);
        }

        // Onboarding
        if (product.getFeatures().getOnboarding()) {
            callUpstreamApi("onboarding", fkn, productCode);
        }

        // PIN Activation
        if (product.getFeatures().getPinActivation()) {
            callUpstreamApi("activate-pin", fkn, productCode);
        }

        // Online Banking Activation
        if (product.getFeatures().getOnlineBankingActivation()) {
            callUpstreamApi("activate-online-banking", fkn, productCode);
        }

        System.out.println("Product onboarding complete for " + product.getName());
    }

    private ProductConfig.Product findProductByCode(String productCode) {
        return productConfig.getItems().stream()
                .filter(product -> product.getProductCode().equalsIgnoreCase(productCode))
                .findFirst()
                .orElse(null);
    }

    private void callUpstreamApi(String action, String fkn, String productCode) {
        String url = API_URLS.get(action) + "?fkn=" + fkn + "&productCode=" + productCode;
        restClient.makePostCall(url, null);
        System.out.println("Called upstream API for action: " + action + " for productCode: " + productCode);
    }
}