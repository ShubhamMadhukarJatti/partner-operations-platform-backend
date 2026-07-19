package com.sharkdom.entity.ppi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sharkdom.constants.ppi.Question_Status;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.organization.Organization;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name="webhook_question_ppi")
@AllArgsConstructor
@NoArgsConstructor
public class WebHookQuestion_Ppi extends BaseEntity {

    private String questionText;
    private Long questionOrder;
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
    private Question_Status status;

    @Transient
    private Boolean applicationReviewTimeAllotted;

    @Transient
    private Boolean PartnerTierAllotted;

    @Transient
    private Boolean discountAllotted;

}
