package com.sharkdom.entity.ppi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.constants.ppi.Question_Status;
import com.sharkdom.constants.ppi.ResponseType_Ppi;
import com.sharkdom.entity.BaseEntity;

import com.sharkdom.entity.organization.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@Table(name="internal_question_ppi")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InternalQuestion_Ppi extends BaseEntity {

    private String questionText;
    private Long questionOrder;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isRequired =false;
    private String helpText;
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long formId;
    @OneToMany(mappedBy = "internalQuestionPpi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Options> options;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "formId")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private FormDetails formDetails;
    @Enumerated(EnumType.STRING)
    private ResponseType_Ppi responseTypePpi;
    @Enumerated(EnumType.STRING)
    private Question_Status status;

    @Transient
    private Boolean isInternalFormActive;

    @Transient
    private Boolean applicationReviewTimeAllotted;

    @Transient
    private Boolean PartnerTierAllotted;

    @Transient
    private Boolean discountAllotted;


}
