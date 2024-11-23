package com.bank.onboarding.services.factory;

import com.bank.onboarding.enums.CustomerType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bank.onboarding.services.ProductService;
import com.bank.onboarding.services.impl.LegalEntityProductService;
import com.bank.onboarding.services.impl.NaturalPersonProductService;

@Component
public class ProductServiceFactory {

    private final NaturalPersonProductService naturalPersonService;
    private final LegalEntityProductService legalEntityService;

    @Autowired
    public ProductServiceFactory(NaturalPersonProductService naturalPersonService, LegalEntityProductService legalEntityService) {
        this.naturalPersonService = naturalPersonService;
        this.legalEntityService = legalEntityService;
    }

    public ProductService getService(CustomerType customerType) {
        switch (customerType) {
            case NATURAL_PERSON:
                return naturalPersonService;
            case LEGAL_ENTITY:
                return legalEntityService;
            default:
                throw new IllegalArgumentException("Unknown customer type: " + customerType);
        }
    }
}
