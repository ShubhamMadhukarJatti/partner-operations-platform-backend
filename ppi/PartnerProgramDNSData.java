package com.sharkdom.entity.ppi;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name ="t_partner_program_dns_data")
@AllArgsConstructor
@NoArgsConstructor
public class PartnerProgramDNSData extends BaseEntity {

    private String customDomain;

    private String targetHost;

    private Long organizationId;

    private String formUrl;

    private String formId;

    private String recordType;

    private String value;

    private String subdomainName;

    private String domain;

    private String txtRecord;

    private String validationToken;

    private String azureDomainResourceName;

    private String txtRecordType;

    private String textRecordValue;

    private boolean isAssociated;

}
