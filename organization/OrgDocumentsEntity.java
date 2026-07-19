package com.sharkdom.entity.organization;

import com.sharkdom.constants.organization.DocumentStatus;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;

@Entity
@Table(name = "org_documents")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class OrgDocumentsEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long organizationId;
    private String documentType;
    private String docUrl;
    private String remarks;
    private DocumentStatus status;

}


