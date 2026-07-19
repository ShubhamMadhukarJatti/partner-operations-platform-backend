package com.sharkdom.model.stripe;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeCardDetailDto {

    private Long id;

    private String brand;

    private String last4;

    private Long expMonth;

    private Long expYear;

    private String country;

    private String status;

    private StripeCustomerDto customer;


}
