package com.sharkdom.entity.ppi;

import com.sharkdom.constants.ppi.FormType;
import com.sharkdom.constants.ppi.Question_Status;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name="formDetails")
@Builder
public class FormDetails extends BaseEntity {

    private Long formId;
    private String form;
    private String formName;
    private Integer version;
    public Question_Status status;
    public FormType formType;
    private Long organizationId;
    private String recentPartner;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isGoogleForm=false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isInternalForm=false;



}
