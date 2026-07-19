package com.sharkdom.profilesection.dto;


import lombok.Data;

import java.util.List;

@Data
public class PartnershipResponse {

    private List<PartnershipScore> topPartnerships;

}
