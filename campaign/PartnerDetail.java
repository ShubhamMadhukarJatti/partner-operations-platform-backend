package com.sharkdom.model.campaign;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerDetail {
    private Long orgId;
    private String orgName;
    private String orgEmail;
}
