package com.sharkdom.requirement.entity;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "t_community_opt_in",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_community_optin_email", columnNames = "contact_email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityOptIn extends BaseEntity {

    @Column(name = "applicant_name",  length = 150)
    private String applicantName;

    @Column(name = "contact_email",  length = 150)
    private String contactEmail;

    @Column(name = "linkedin_url", columnDefinition = "TEXT")
    private String linkedinUrl;

    @Column(name = "job_title",  length = 150)
    private String jobTitle;

    @Column(name = "preferred_location",  length = 100)
    private String preferredLocation;

    @Column(name = "additional_information", columnDefinition = "TEXT")
    private String additionalInformation;

    @Column(name = "license_pdf_url", columnDefinition = "TEXT")
    private String licensePdfUrl;
}