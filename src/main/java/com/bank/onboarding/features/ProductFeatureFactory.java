package com.bank.onboarding.features;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bank.onboarding.features.impl.OnboardingFeatureImpl;
import com.bank.onboarding.features.impl.OnlineBankingActivationFeatureImpl;
import com.bank.onboarding.features.impl.PinActivationFeatureImpl;

@Component
public class ProductFeatureFactory {

    private final OnboardingFeatureImpl onboardingFeature;
    private final PinActivationFeatureImpl pinActivationFeature;
    private final OnlineBankingActivationFeatureImpl onlineBankingFeature;

    @Autowired
    public ProductFeatureFactory(OnboardingFeatureImpl onboardingFeature, PinActivationFeatureImpl pinActivationFeature, OnlineBankingActivationFeatureImpl onlineBankingFeature) {
        this.onboardingFeature = onboardingFeature;
        this.pinActivationFeature = pinActivationFeature;
        this.onlineBankingFeature = onlineBankingFeature;
    }

    public ProductFeature getFeature(String featureType) {
        switch (featureType) {
            case "onboarding":
                return onboardingFeature;
            case "activate-pin":
                return pinActivationFeature;
            case "activate-online-banking":
                return onlineBankingFeature;
            default:
                throw new IllegalArgumentException("Unknown feature type: " + featureType);
        }
    }
}
