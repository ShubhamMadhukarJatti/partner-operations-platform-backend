package com.sharkdom.agenticai.model;


import lombok.Data;

import java.util.List;

@Data
public class ForecastResponse {

    private String name;

    private String icon;

    private Integer match_score;

    private Double revenue_influenced;

    private String time_to_first_partner_revenue;

    private List<String> key_alignment;

    private String risk;

}
