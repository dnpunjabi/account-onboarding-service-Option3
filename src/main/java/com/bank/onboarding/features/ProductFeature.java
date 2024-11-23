package com.bank.onboarding.features;

import java.util.Map;

public interface ProductFeature {
    void execute(Map<String, Object> requestContext) throws Exception;
}