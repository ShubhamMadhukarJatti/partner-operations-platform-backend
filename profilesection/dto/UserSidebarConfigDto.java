package com.sharkdom.profilesection.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSidebarConfigDto {

    private String userId;
    private List<String> pinnedItemHrefs;
    private List<String> sidebarItemHrefs;
    private Map<String, Boolean> openNestedItems;
    private Boolean isCollapsed;
    private Boolean isPartnerView;
    private Boolean isVendorView;
}