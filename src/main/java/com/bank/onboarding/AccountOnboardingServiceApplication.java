package com.bank.onboarding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.bank.onboarding.config.ProductConfig;

@SpringBootApplication
@EnableConfigurationProperties(ProductConfig.class)
public class AccountOnboardingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccountOnboardingServiceApplication.class, args);
	}

}
