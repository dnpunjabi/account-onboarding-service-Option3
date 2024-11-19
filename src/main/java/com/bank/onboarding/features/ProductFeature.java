package com.bank.onboarding.features;

public interface ProductFeature {
    void execute(String fkn, String productCode, boolean simulateFailure) throws Exception;
}