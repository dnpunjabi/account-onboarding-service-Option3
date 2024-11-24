package com.bank.onboarding.services.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bank.onboarding.features.impl.AccountOpeningFeatureImpl;
import com.bank.onboarding.features.impl.OnlineBankingActivationFeatureImpl;
import com.bank.onboarding.features.impl.PinActivationFeatureImpl;
import com.bank.onboarding.features.impl.SchufaCheckFeatureImpl;
import com.bank.onboarding.services.CustomerProductService;
import com.bank.onboarding.services.OrinocoCaseManagementService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NaturalPersonCurrentAccountService implements CustomerProductService {

    private final SchufaCheckFeatureImpl schufaCheckFeature;
    private final AccountOpeningFeatureImpl accountOpeningFeature;
    private final PinActivationFeatureImpl pinActivationFeature;
    private final OnlineBankingActivationFeatureImpl activateOnlineBankingFeature;
    private final OrinocoCaseManagementService caseManagementService;

    @Autowired
    public NaturalPersonCurrentAccountService(SchufaCheckFeatureImpl schufaCheckFeature,
                                              AccountOpeningFeatureImpl accountOpeningFeature,
                                              PinActivationFeatureImpl pinActivationFeature,
                                              OnlineBankingActivationFeatureImpl activateOnlineBankingFeature,
                                              OrinocoCaseManagementService caseManagementService) {
        this.schufaCheckFeature = schufaCheckFeature;
        this.accountOpeningFeature = accountOpeningFeature;
        this.pinActivationFeature = pinActivationFeature;
        this.activateOnlineBankingFeature = activateOnlineBankingFeature;
        this.caseManagementService = caseManagementService;
    }

    @Override
    public String process(Map<String, Object> requestContext) throws Exception {
        try {
            // Step 1: SCHUFA Check
            schufaCheckFeature.execute(requestContext);
            // Step 2: Account Opening
            accountOpeningFeature.execute(requestContext);
            // Step 3: PIN Activation
            boolean pinSet = (boolean) requestContext.getOrDefault("pinSet", false);
            if (pinSet) {
                pinActivationFeature.execute(requestContext);
            }
            // Step 4: Online Banking Activation
            boolean onlineBankingOptIn = (boolean) requestContext.getOrDefault("onlineBankingOptIn", false);
            if (onlineBankingOptIn) {
                activateOnlineBankingFeature.execute(requestContext);
            }
            log.info("Successfully completed onboarding for Natural Person Current Account.");
            return null;
        } catch (Exception ex) {
            return handleFailure(requestContext, ex.getMessage());
        }
    }

    private String handleFailure(Map<String, Object> requestContext, String errorDetails) {
        log.error("Failure occurred during onboarding: {}", errorDetails);
        return caseManagementService.notifyBackOffice(requestContext, errorDetails);
    }
}