package com.sharkdom.dto;

import lombok.Data;

@Data
public class AddressContactUpdateRequest {

    private String address;
    private String city;
    private String zipCode;
    private String state;
    private String country;
    private String phone;
}