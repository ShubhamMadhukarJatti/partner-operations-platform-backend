package com.sharkdom.tablecustomization.dto;

import com.sharkdom.tablecustomization.entity.externalpartner.ExternalPartnerTableColumn;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrgColumnsResponse {

    private List<String> externalDocColumns;

    private List<ExternalPartnerTableColumn> externalPartnerColumns;

}