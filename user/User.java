package com.sharkdom.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.util.Util;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.constants.user.Gender;
import com.sharkdom.constants.user.UserStatus;
import lombok.*;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "users", indexes = {@Index(columnList = "userId", name = "user_userId"),
        @Index(columnList = "username", name = "user_username"),
        @Index(columnList = "last_login_time", name = "idx_user_last_login_time")})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class User extends BaseEntity  {

    private static final long serialVersionUID = 1L;

    @Column(unique = true)
    private String userId;
    @Column(unique = true)
    private String username;
    private String name;
    private Gender gender;
    private String mobile;
    @Column(unique = true)
    private String email;
    private String briefDescription;
    private String about;
    private boolean emailVerified;
    private String userType;
    private Integer riskAppetite;
    private Integer mint;
    private String status = UserStatus.ACTIVE.toString();
    private String deviceId;
    private String tags;
    private String city;
    private String state;
    private boolean canCollaborate;
    private String website;
    private boolean trialPeriodProcured;
    private String sector;
    private boolean isOnboarded;
    @Transient
    private String errorMessage;

    @Column(name = "user_subtype")
    private String userSubtype;

    @JsonIgnore
    @Column(name="password")
    private String password;


    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_fk")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<Role> roles;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_fk")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<WorkExperience> workExperiences;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_fk")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<InterestAreas> interestAreas;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_fk")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<UserAdditionalDetails> additionalDetails;


    @PreUpdate
    @PrePersist
    public void prePersist() {
        sortTags();
    }

    // Sort all tags and remove leading/trailing whitespace from tags before
    // persisting them into DB
    public void sortTags() {
        this.tags = Util.sortCommaSeparatedString(this.tags, ",", ",");
    }

    @JsonProperty("is_continue_free_deal")
    @Column(name = "is_continue_free_deal", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isContinueFreeDeal;

    @JsonProperty("is_continue_frree_partner_mapping")
    @Column(name = "is_continue_free_partner_mapping", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isContinueFreePartnerMapping;

    @Column(name="is_team_member_user", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isTeamMemberUser=false;

    @JsonIgnore
    @Column(name="last_login_time")
    private Instant lastLoginTime;

    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;
}
