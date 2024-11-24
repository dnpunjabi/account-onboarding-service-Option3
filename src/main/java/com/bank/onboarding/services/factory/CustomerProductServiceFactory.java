package com.bank.onboarding.services.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bank.onboarding.enums.CustomerType;
import com.bank.onboarding.enums.ProductType;
import com.bank.onboarding.services.CustomerProductService;
import com.bank.onboarding.services.impl.LegalEntityCurrentAccountService;
import com.bank.onboarding.services.impl.LegalEntityFlexAccountService;
import com.bank.onboarding.services.impl.NaturalPersonFlexAccountService;
import com.bank.onboarding.services.impl.NaturalPersonCurrentAccountService;

@Component
public class CustomerProductServiceFactory {

    private final NaturalPersonCurrentAccountService naturalPersonCurrentAccountService;
    private final NaturalPersonFlexAccountService naturalPersonFlexAccountService;
    private final LegalEntityCurrentAccountService legalPersonCurrentAccountService;
    private final LegalEntityFlexAccountService legalPersonFlexAccountService;

    @Autowired
    public CustomerProductServiceFactory(NaturalPersonCurrentAccountService naturalPersonCurrentAccountService,
                                         NaturalPersonFlexAccountService naturalPersonFlexAccountService,
                                         LegalEntityCurrentAccountService legalPersonCurrentAccountService,
                                         LegalEntityFlexAccountService legalPersonFlexAccountService) {
        this.naturalPersonCurrentAccountService = naturalPersonCurrentAccountService;
        this.naturalPersonFlexAccountService = naturalPersonFlexAccountService;
        this.legalPersonCurrentAccountService = legalPersonCurrentAccountService;
        this.legalPersonFlexAccountService = legalPersonFlexAccountService;
    }

    public CustomerProductService getService(CustomerType customerType, ProductType productType) {
        if (customerType == CustomerType.NATURAL_PERSON && productType == ProductType.CURRENT_ACCOUNT) {
            return naturalPersonCurrentAccountService;
        } else if (customerType == CustomerType.NATURAL_PERSON && productType == ProductType.FLEX_ACCOUNT) {
            return naturalPersonFlexAccountService;
        } else if (customerType == CustomerType.LEGAL_ENTITY && productType == ProductType.CURRENT_ACCOUNT) {
            return legalPersonCurrentAccountService;
        } else if (customerType == CustomerType.LEGAL_ENTITY && productType == ProductType.FLEX_ACCOUNT) {
            return legalPersonFlexAccountService;
        }

        throw new IllegalArgumentException("No service found for customer type: " + customerType +
                                           " and product type: " + productType);
    }
}