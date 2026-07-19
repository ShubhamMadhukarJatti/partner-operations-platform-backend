package com.sharkdom.entity.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "overlap_records_fields")
@Data
public class OverlapRecordFieldEntity extends BaseEntity {

    // ---------------- Existing Fields ----------------
    private String name;
    private String companyName;
    private String contactEmail;
    private String domain;
    private String dealStage;
    private String creationDate;
    private String closeDate;
    private String subscribed;
    private String ticketSize;

    // ---------------- New Company Fields ----------------
    private String website;
    private String industry;
    private String companySize;
    private String country;
    private String linkedinUrl;
    private String annualRevenue;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String companyPhone;
    private String city;

    // ---------------- New Contact Fields ----------------
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String contactLinkedinUrl;
    private String leadStatus;
    private String contactPhone;
    private String lastActivityDate;
    private String contactOwner;

    @JsonIgnore
    private String associatedCompanyId;

    // ---------------- New Deal Fields ----------------
    private String dealName;
    private String dealOwner;
    private String amountAcv;

    @JsonIgnore
    private String dealId;

    private String pipeline;
    private String dealType;

    @JsonIgnore
    private String associatedContactId;

    @ManyToOne
    @JoinColumn(name = "overlap_record_id")
    @JsonIgnore
    private OverlapRecordEntity overlapRecord;

    @Column(nullable = false)
    private Integer version;

    private Integer versionId;
}