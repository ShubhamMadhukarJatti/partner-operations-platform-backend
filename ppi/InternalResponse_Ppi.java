package com.sharkdom.entity.ppi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.sharkdom.constants.ppi.FormStatus;
import com.sharkdom.constants.ppi.ResponseType_Ppi;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.organization.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@Table(name="internal_response_ppi")
@AllArgsConstructor
@NoArgsConstructor
public class InternalResponse_Ppi extends BaseEntity {

    private Long questionId;
    private Long formId;
    private List<String> responseText;

    @Enumerated(EnumType.STRING)
    private ResponseType_Ppi responseTypePpi;
    @JsonManagedReference
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    @OneToMany(mappedBy = "internalResponsePpi", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Options> options;
    private String userId;
    private FormStatus formStatus;

    private String brandName;
    private String email;
    private String username;

    @Column(name="is_external_submission", columnDefinition = "boolean default false")
    private Boolean isExternalSubmission=false;

}

