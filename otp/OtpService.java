package com.sharkdom.service.otp;

import com.github.benmanes.caffeine.cache.Cache;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.user.UserRole;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final EmailService emailService;

    private final Cache<String, String> otpCache;

    private final OrganizationRepository organizationRepository;

    private final UserRepository userRepository;

    Random random = new Random();

    public Map<String, String> generateOTP(String key, String templateCode) {
        try {
            return generateNumbers(key, templateCode);
        } catch (Exception e) {
            log.error("Something went wrong while otp generation");
            throw new SharkdomException(ErrorMessages.SH151, e.getMessage());
        }
    }

    public Map<String, String> generateNumbers(String key, String templateCode) throws Exception {
        String correctOTP = String.format("%06d", random.nextInt(1000000));
        emailService.sendOtpByEmail(key, correctOTP, templateCode);
        storeOTP(key, correctOTP);
        return Map.of("message", "OTP sent successfully");
    }

    public void storeOTP(String key, String correctOTP) {
        otpCache.put(key, correctOTP);
    }

    public Map<String, Boolean> validateOTP(String key, String selectedOTP) {
        Map<String, Boolean> returnOutput = new ConcurrentHashMap<>();
        String correctOTP = otpCache.getIfPresent(key);
        if (selectedOTP.equals(correctOTP)) {
            otpCache.invalidate(key);
            organizationRepository.findByPrimaryEmail(key)
                    .ifPresent(foundOrganization -> {
                        foundOrganization.setPrimaryEmailVerified("true");
                        organizationRepository.save(foundOrganization);
                        log.info("Organization updated the primary_email_verified");
                    });
            User foundUser = userRepository.findByEmail(key)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH78, key));
            foundUser.setEmailVerified(true);
            userRepository.save(foundUser);
            log.info("User updated the email_verified");
            returnOutput.put("validate", Boolean.TRUE);
            return returnOutput;
        }
        throw new ServiceException(ErrorMessages.SH06);
    }

    public void validateOTPV1(String key, String selectedOTP) {
        String correctOTP = otpCache.getIfPresent(key);
        if (selectedOTP != null && selectedOTP.equals(correctOTP)) {
            otpCache.invalidate(key);
            log.info("OTP validated successfully for key: {}", key);
        } else {
            throw new ServiceException(ErrorMessages.SH06); // Invalid OTP
        }
    }

    public void validatePartnerOTP(String key, String selectedOTP) {
        String correctOTP = otpCache.getIfPresent(key);

        if (selectedOTP != null && selectedOTP.equals(correctOTP)) {
            otpCache.invalidate(key);

            userRepository.findByEmailAndIsActiveTrue(key)
                    .ifPresent(user -> {
                        user.setEmailVerified(true);
                        user.setUserType(UserRole.PARTNER.name());
                        userRepository.save(user);

                        log.info("Partner user email verified successfully: {}", key);
                    });

            return;
        }

        throw new ServiceException(ErrorMessages.SH06);
    }


}
