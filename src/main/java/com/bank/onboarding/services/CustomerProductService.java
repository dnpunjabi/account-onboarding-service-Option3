package com.bank.onboarding.services;

import java.util.Map;

public interface CustomerProductService {
    String process(Map<String, Object> requestContext) throws Exception;
}
