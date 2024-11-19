package com.bank.onboarding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {
    private String fkn;           // Customer Identifier (FKN)
    private String productCode;   // Product Code to be onboarded
    private boolean simulateFailure = false; // Flag to simulate failure (default: false)
}
