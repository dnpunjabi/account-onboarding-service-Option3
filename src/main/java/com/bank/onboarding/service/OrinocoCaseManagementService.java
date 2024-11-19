package com.bank.onboarding.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bank.onboarding.dto.OrinocoCasePayload;
import com.bank.onboarding.util.RestClientUtilDummy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrinocoCaseManagementService {

    private final RestClientUtilDummy restClientUtil;

    @Autowired
    public OrinocoCaseManagementService(RestClientUtilDummy restClientUtil) {
        this.restClientUtil = restClientUtil;
    }

    /**
     * Notify the back office through Orinoco Case Management API.
     * @param casePayload The payload containing details about the failure.
     * @return A dummy case ID to return to the client.
     */
    public String notifyBackOffice(OrinocoCasePayload casePayload) {
        log.info("Notifying Orinoco Case Management system with payload: {}", casePayload);
        try {
            // Call the Orinoco Case Management API to notify the back office
            ResponseEntity<String> response = restClientUtil.makeCaseManagementCall("https://orinoco.api/case-management", casePayload);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully notified the Orinoco Case Management system.");
                // Generate a dummy case ID
                String caseId = "CASE-" + System.currentTimeMillis();
                return caseId;
            } else {
                log.error("Failed to notify the Orinoco Case Management: {}", response.getBody());
            }

        } catch (Exception ex) {
            log.error("Error occurred while notifying Orinoco Case Management: {}", ex.getMessage());
        }

        return "ERROR-CASE";  // Return a fallback case ID if something fails
    }
}