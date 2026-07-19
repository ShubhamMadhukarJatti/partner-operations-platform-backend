package com.sharkdom.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeneralUtils {

    /**
     * append value of initialAppendValue at the beginning of all keys
     *
     * @param object
     * @param initialAppendValue
     * @return
     */
    public Map<String, Object> convetObjectToMap(Object object, String initialAppendValue) {
        try {
            return ((Map<String, Object>) new ObjectMapper().convertValue(object, Map.class)).entrySet().stream().map(entry ->
                    new HashMap.SimpleEntry<>(initialAppendValue + entry.getKey(), (entry.getValue() == null) ? "" : entry.getValue())
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            log.error("Failed to convert object to map " + e.getMessage());
        }
        return Map.of();
    }

    public static String generateVerificationToken(int length) {
        byte[] tokenBytes = new byte[length];
        new SecureRandom().nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public static UriComponentsBuilder generateVerificationLink(String baseUrl, String verificationToken, String transaction) {
        return UriComponentsBuilder.fromHttpUrl(baseUrl + "verify")
                .queryParam("transactionId", transaction)
                .queryParam("code", verificationToken);
    }

    public static Date calculateExpirationTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 48);
        return calendar.getTime();
    }

    public static Date calculateCodeExpirationTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 20);
        return calendar.getTime();
    }

    public static String generateRandomCode() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }
}
