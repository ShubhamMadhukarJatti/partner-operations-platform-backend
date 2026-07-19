package com.sharkdom.util;

import com.sharkdom.constants.Constants;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.security.CustomUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

public class Util {
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final String ALGORITHM = "AES/GCM/NoPadding";

    public static String getAppName() {
        return "Hey!!";
    }

    public static String sortCommaSeparatedString(String str, String presentDelimeter, String newDelimeter) {

        if (null == str) {
            return "";
        }
        if (str.indexOf(presentDelimeter) < 0) {
            return str;
        }

        String[] tagsArray = str.split(presentDelimeter);
        Arrays.parallelSetAll(tagsArray, (i) -> tagsArray[i].trim());
        Arrays.sort(tagsArray);
        return String.join(newDelimeter, tagsArray);

    }

    public static int getOrganizationProgress(Organization organization) {
        int organizationProgress = 0;
        if (StringUtils.isNotEmpty(organization.getWebsite())) {
            organizationProgress += 20;
        }
        if (StringUtils.isNotEmpty(organization.getLegalName())) {
            organizationProgress += 20;
        }
        if (StringUtils.isNotEmpty(organization.getTargetMarket())) {
            organizationProgress += 20;
        }
        if (StringUtils.isNotEmpty(organization.getCompanyType())) {
            organizationProgress += 20;
        }
        if (!Objects.equals(organization.getLogoUrl(), Constants.PLACEHOLDER_LOGO)) {
            organizationProgress += 20;
        }
        return organizationProgress;
    }

    public static String encryptForDatabase(String plaintext, String aesKey) {
        try {
            SecretKey secretKey = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            // Initialize cipher for encryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.SH131, e.getMessage());
        }
    }

    public static String decryptFromDatabase(String encryptedData, String aesKey) {
        try {
            // Get the key
            SecretKey secretKey = new SecretKeySpec(aesKey.getBytes(StandardCharsets.UTF_8), "AES");

            // Decode from Base64
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

            // Extract IV and ciphertext
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt
            byte[] decryptedBytes = cipher.doFinal(ciphertext);

            // Return plaintext
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new ServiceException(ErrorMessages.SH132, e.getMessage());
        }
    }

    public static LocalDate convertToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Long getOrgIdFromToken() {
       CustomUserDetails authentication= (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return authentication.getOrganizationId();
    }

    public static String getUserFromToken() {
        CustomUserDetails authentication= (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return authentication.getUsername();
    }

//    public static String getUserIdFromToken() {
//        CustomUserDetails authentication= (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        return authentication.get
//    }
}
