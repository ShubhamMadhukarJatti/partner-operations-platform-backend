package com.sharkdom.entity.ppi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.organization.Organization;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name="webhook_response_ppi")
@AllArgsConstructor
@NoArgsConstructor

public class WebHookResponse_Ppi extends BaseEntity {


    private Long questionId;
private String submissionId;

    @Column(name = "form_id") // maps to DB column
    private String formId;
    private List<String> responseText;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
}
