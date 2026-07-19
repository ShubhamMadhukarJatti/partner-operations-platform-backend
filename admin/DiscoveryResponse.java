package com.sharkdom.model.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DiscoveryResponse {
    private String organizationName;
    private String logoUrl;
    private boolean existInOrganization;

}
