package com.sharkdom.AIpartnerPulse.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailDomainService {

    /**
     * Extracts the website link from an email address.
     * Example: "john.doe@gmail.com" → "https://gmail.com"
     *
     * @param email user's email address
     * @return website link, or null if email is invalid
     */
    public String getWebsiteFromEmail(String email) {
        log.info("Received request to extract website from email: {}", email);
        if (email == null || !email.contains("@")) {
            log.warn("Invalid email format: {}", email);
            return null;
        }
        try {
            String domain = email.substring(email.indexOf('@') + 1);
            domain = domain.trim().toLowerCase();

            String website = "https://" + domain;
            log.debug("Extracted domain: {}", domain);
            log.info("Generated website link: {}", website);

            return website;
        } catch (Exception e) {
            log.error("Error extracting website from email: {}", email, e);
            return null;
        }
    }
}