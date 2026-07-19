package com.sharkdom.requirement.entity;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_talent_networks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TalentNetwork extends BaseEntity {

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "website_url", columnDefinition = "TEXT")
    private String websiteUrl;

    @Column(name = "contact_email", columnDefinition = "TEXT")
    private String contactEmail;

    @Column(name = "contact_phone_number", columnDefinition = "TEXT")
    private String contactPhoneNumber;

    @Column(name = "job_title", columnDefinition = "TEXT")
    private String jobTitle;

    @Column(name = "preferred_location", columnDefinition = "TEXT")
    private String preferredLocation;

    @Column(name = "role_summary", columnDefinition = "TEXT")
    private String roleSummary;

    @Column(name = "linkedin_url", columnDefinition = "TEXT")
    private String linkedinUrl;

    @Column(name = "minimum_years_of_experience")
    private Integer minimumYearsOfExperience;

    @Column(name = "use_screening_bot")
    private Boolean useScreeningBot = false;

    @Column(name = "response_time", length = 50)
    private String responseTime;
}