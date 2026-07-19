package com.sharkdom.partnerprogram.dtos;

import com.sharkdom.partnerprogram.enums.GtmFocusType;
import com.sharkdom.partnerprogram.enums.PartnershipTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartnerApplicationDTO {

    private Long id;

    private String fullName;

    private String email;

    private String userId;

    private String linkedInProfileUrl;

    private String companyName;

    private String geography;

    private String companiesAdvised;

    private Set<GtmFocusType> primaryGtmFocus;

    private String howDidYouHearAboutProgram;

    private PartnershipTier partnershipTier;

    private String networkDescription;

    private boolean isActive;

    private String referCode;
}
