package com.sharkdom.entity.organization;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sharkdom.constants.organization.*;
import com.sharkdom.constants.ppi.FormType;
import com.sharkdom.converter.RegionConverter;
import com.sharkdom.converter.ServingCustomersTypeListConverter;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.credits.Credits;
import com.sharkdom.entity.meetings.Schedule;
import com.sharkdom.entity.ppi.FormApplyStatus;
import com.sharkdom.entity.ppi.InternalResponse_Ppi;
import com.sharkdom.model.organization.OrganizationCollaborationResponse;
import com.sharkdom.model.organization.RoleSpecs;
import com.sharkdom.util.jsonconvertes.StringListToStringConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serial;
import java.util.*;

@Entity
@Table(name = "organization", indexes = {@Index(columnList = "code", name = "org_code"),
        @Index(columnList = "name", name = "org_name")})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Organization extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    List<OrganizationServices> services;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    List<OrganizationSocialMedia> socialMedias;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String about;
    @Column(columnDefinition = "TEXT")
    private String briefDescription;
    @Schema(example = "2022")
    private Integer inceptionYear;
    private String registrationType;
    private String sector;
    private String stage;
    private String pincode;

    private String dateOfIncorporation;
    private String verificationApplicationStatus;


    private String city;
    private String state;
    private String additionalDetails;
    @Email(message = "email is not valid")
    @NotEmpty(message = "primaryEmail can not be empty")
    @Column(unique = true)
    private String primaryEmail;
    private String primaryEmailVerified;
    private String domain;
    private String domainVerified;
    private String website;
    private OrganizationStatus status;
    private boolean verified = false;
    private String verifiedBy;
    private Date verifiedOn;
    private String address;
    private String companyType;
    private String legalName;
    private String contactNumber;
    private String targetMarket;
    private String funding;

    @Column(name = "is_in_house_partnership")
    private Boolean isInHousePartnership;

    @Column(name = "partnership_team_size")
    @Enumerated(EnumType.STRING)
    private TeamSize partnershipTeamSize;

    @Column(name = "goals_to_use_sharkdom")
    @Convert(converter = StringListToStringConverter.class)
    private List<String> goalsToUseSharkdom;

    @Column(name = "ignore_fund_raising")
    private Boolean ignoreFundRaising;

    @Column(name = "served_customers")
    @Convert(converter = ServingCustomersTypeListConverter.class)
    private List<ServingCustomersType> servedCustomers;


    @Column(name = "customer_base")
    private String customerBase;


    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_fk")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<PreferredPartnershipTypes> preferredPartnershipTypes;

    private String partnershipRestrictions;
    private String cin;
    private Date lastActivityAtTimestamp;
    private String verificationResponse;
    private Source source;
    private Date incorporationDate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_fk")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<PreferredSector> preferredSectors;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Signatory> signatories;
    private boolean openForPartnership=true;
    @Schema(defaultValue = "false")
    private boolean isSubscribed = false;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Credits credits;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "meeting_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Schedule> schedules;
    private boolean referralProgram=false;
    private boolean brandResources=false;
    private String documentPath;
    @Transient
    private List<OrganizationCollaborationResponse> organizationCollaborations;
    private Long acknowledgmentTime;
    private Long activePartnerships;
    private Long pipelinePartnerships;
    private Long meetingSuccessRate;
    private String referralCode;
    @Transient
    String planCode;
    private String logoUrl;

    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<PreferredSubSector> preferredSubSectors;
    private boolean openPilotProgram=false;
    private Double rating;
    private boolean isSubSector=false;
    private RoleSpecs roleSpecs;
    private String sectorType;
    private boolean emailUnsubscribed = false;
    private String onboardedPartners;
    private String country;
    private String signUpSource;
    private Set<ExcludeCompanySize> excludeCompanySize;
    private Set<ExcludePartnershipGoals> excludePartnershipGoals;
    private Set<AvoidGeographicRegion> avoidGeographicRegion;
    private Set<ExcludeBusinessMaturityLevel> excludeBusinessMaturityLevel;
    private Set<ExcludeCompaniesTechnology> excludeCompaniesTechnology;
    private boolean isCompanySixMonthOperation;
    private boolean isCompanyFundRaising;
    @Column(name="Is_unverified_deal")
    private boolean isUnverifiedDeal;

    private FormType formType;
    private String form ;
    private String responderUrl;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isInternalFormActive;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isGoogleFormActive;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFormSubmitted =false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isApplied =false;

    private String formName;
    @JsonBackReference
    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private List<InternalResponse_Ppi> responses;
    @OneToMany(mappedBy = "formOwnerOrg", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormApplyStatus> formApplications = new ArrayList<>();

    private String referralSource;

    public void setCode(String code) {
        if (null != code) {
            this.code = code.toLowerCase();
        }
    }

    @Column(name = "region_to_partner_with")
    @Convert(converter = RegionConverter.class)
    private List<RegionToPartnerWith> regionToPartnerWith;

    @Column(name = "is_claimed", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isClaimed = false;

    @Column(name="poc")
    private String poc;

    @Column(name = "filters")
    @Convert(converter = StringListToStringConverter.class)
    private List<String> filters;

    @Column(name = "compliances")
    @Convert(converter = StringListToStringConverter.class)
    private List<String> compliances;

    @Column(name = "filters_added",columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean filtersAdded;

    @Column(name = "currency",columnDefinition = "VARCHAR(10) DEFAULT 'INR'")
    private String currency="INR";

    @Column(name="product_url",columnDefinition = "TEXT")
    private String productUrl="https://sharkdom.com/product/";


    @Transient
    private boolean isSelectedForExternalPartnerships;

    @Transient
    private boolean isShortlisted;

    @Transient
    private boolean isExternalPartnerImported;

    @Transient
    private boolean isEmailOutreachConsentGranted;

    @Transient
    private boolean isDealCreatedOrAssigned;

    @Transient
    private Long sendProposalCount;

    @Transient
    private boolean anyIntegrationAdded;

    @Transient
    private boolean isFreeDealPlan;

    @Transient
    private String isFreePlanClaimedUserName;

    @Transient
    private boolean isFreePartnerMappingPlan;

    @Transient
    private boolean isFreePartnerMappingPlanClaimedUserName;

    @Column(name="top_partner",columnDefinition = "BIGINT DEFAULT 0")
    private Long topPartner=0L;

    @Column(name="most_popular",columnDefinition = "BIGINT DEFAULT 0")
    private Long mostPopular=0L;

    @Column(name="market_place_record",columnDefinition = "BIGINT DEFAULT 0")
    private Long marketPlaceRecord=0L;

    @Column(columnDefinition = "TEXT")
    private String tagLine;

    @Column(name = "headquarter")
    private String headquarter;

    @Column(name = "founded_in")
    private String foundedIn;

    @Column(name = "industries",columnDefinition = "TEXT")
    private String industries;

    @Column(name = "trustpilot_rating")
    private Double trustpilotRating;

    @Column(name = "trustpilot_total_reviews")
    private Integer trustpilotTotalReviews;
}


