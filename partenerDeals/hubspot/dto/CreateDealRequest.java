package com.sharkdom.service.partenerDeals.hubspot.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CreateDealRequest {

    private Map<String, String> properties;

}
