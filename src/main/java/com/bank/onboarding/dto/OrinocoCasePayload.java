package com.bank.onboarding.dto;

import lombok.Data;

@Data
public class OrinocoCasePayload {
    private String fkn;             // Customer Identifier (FKN)
    private String productCode;     // The product code that failed
    private String failureMessage;  // The reason for the failure (error message)
}
