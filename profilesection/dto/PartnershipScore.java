package com.sharkdom.profilesection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PartnershipScore {

    private String type;
    private Integer percentage;

}