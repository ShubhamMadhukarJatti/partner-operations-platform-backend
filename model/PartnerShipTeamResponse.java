package com.sharkdom.agenticai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerShipTeamResponse {

    private String name;
    private String linkedin;
    private String title;
    private String email;
}