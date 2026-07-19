package com.sharkdom.entity.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "t_extnernal_overlap_records_field")
@Data
public class ExternalPartnerOverlapRecordFieldEntity extends BaseEntity {
    private String name;
    private String companyName;
    private String contactEmail;
    private String domain;
    private String dealStage;
    private String creationDate;
    private String closeDate;
    private String subscribed;
    private String ticketSize;

    @ManyToOne
    @JoinColumn(name = "overlap_record_id")
    @JsonIgnore
    private ExternalPartnerOverlapRecordEntity overlapRecord;
}
