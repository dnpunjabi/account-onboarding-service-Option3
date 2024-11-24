package com.bank.onboarding.enums;

public enum ProductType {
    CURRENT_ACCOUNT, // BCA - Current Account
    FLEX_ACCOUNT;    // FGA - Flex Account

    public static ProductType fromCode(String code) {
        return switch (code.toUpperCase()) {
            case "BCA" -> CURRENT_ACCOUNT;
            case "FGA" -> FLEX_ACCOUNT;
            default -> throw new IllegalArgumentException("Invalid product type code: " + code);
        };
    }
}
