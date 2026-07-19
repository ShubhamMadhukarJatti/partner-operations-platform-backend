package com.sharkdom.entity.partenearDeals;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.partnerDeals.DealSource;
import com.sharkdom.constants.partnerDeals.DealStage;
import com.sharkdom.constants.partnerDeals.DealStatus;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.partnerDeals.CustomFieldDto;
import jakarta.persistence.*;
import lombok.Data;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "deals")
@Data
public class Deal extends BaseEntity {

    @Column(nullable = false)
    private String customerAccountName;

    @Column(nullable = false)
    private String dealId;

    @Column(nullable = false)
    private String dealCode;

    @Column(nullable = false)
    private String website;

    @Column(nullable = false)
    private String headQuarterLocation;

    @Column(nullable = false)
    private Integer estimatedAcv;

    @Column(nullable = false)
    private Integer expectedClosingTime;

    @Column(nullable = false)
    private String currentSolution;

    @Column(nullable = false,columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String customFields;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealStage dealStage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealSource source;

    @Column(nullable = false)
    private Boolean isApproved;

    @Column(nullable = false)
    private Long dealerOrgId;

    @Column(nullable = false)
    private Long vendorOrgId;

    @Column(nullable = false)
    private Long dealProtectionPeriod;
    @Column(nullable = false)
    private Boolean isSent ;
    @Column(nullable = false)
    private DealStatus dealStatus;
    @Column(nullable = false)
    private String dealSize;

    private String hotspotDealId;

    @Column(name = "lastUpdatedTimestamp")
    private Date lastUpdatedTimestamp;

    @Column(name = "last_activity")
    private String lastActivity;

    @Column(name="point_of_contact")
    private String pointOfContact;

    @Column(name="salesforce_deal_ids", columnDefinition = "TEXT")
    private String salesforceDealId;

    @Column(name="zoho_deal_ids", columnDefinition = "TEXT")
    private String zohoDealId;

    @Column(name = "provider")
    private String provider;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "external_partner_code")
    private String externalPartnerCode;

    @Column(name="is_external_partner_portal_deal",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isExternalPartnerPortalDeal;

    @Column(name="is_internal_to_external_partner_portal_deal",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isInternalToExternalPartnerPortalDeal;

    @Transient
    public Map<String, CustomFieldDto> getCustomFieldsMap() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(customFields, new TypeReference<Map<String, CustomFieldDto>>() {});
        } catch (IOException e) {
            return new HashMap<>();
        }
    }
}
