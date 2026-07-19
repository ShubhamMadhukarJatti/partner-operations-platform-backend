package com.sharkdom.model.organizatiocollaboration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class PartnerResponse {
    private Long id;
    private Long organizationId;
    private String status;
    private PartnershipType type;
    private Date creationTimestamp;
    private String name;
    private String description;
    private String logoUrl;
}
