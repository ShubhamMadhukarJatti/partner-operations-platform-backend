package com.sharkdom.partnerprogram.entities;

import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.UUID;

@Service
public class ReferralCodeUtil {

    /**
     * UUID -> Short Referral Code
     */
    public String generateReferralCode(String userId) {
        UUID uuid = UUID.fromString(userId);

        BigInteger bigInt = uuidToBigInteger(uuid);

        // Base36 = 0-9 + A-Z
        return bigInt.toString(36).toUpperCase();
    }

    /**
     * Referral Code -> Original UUID
     */
    public String decodeReferralCode(String referralCode) {
        BigInteger bigInt = new BigInteger(
                referralCode.toLowerCase(),
                36
        );

        return bigIntegerToUUID(bigInt).toString();
    }

    private BigInteger uuidToBigInteger(UUID uuid) {
        return BigInteger.valueOf(uuid.getMostSignificantBits())
                .shiftLeft(64)
                .add(BigInteger.valueOf(uuid.getLeastSignificantBits())
                        .and(BigInteger.valueOf(Long.MAX_VALUE)));
    }

    private static UUID bigIntegerToUUID(BigInteger bigInt) {
        long mostSigBits = bigInt.shiftRight(64).longValue();
        long leastSigBits = bigInt.longValue();

        return new UUID(mostSigBits, leastSigBits);
    }
}