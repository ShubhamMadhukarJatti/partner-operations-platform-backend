package com.sharkdom.subscription.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionPlanFreeTrailRequest {

    @NotNull(message = "Number of seats is required")
    @Positive(message = "Number of seats must be greater than 0")
    private Long numberOfSeats;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotBlank(message = "Organization name is required")
    private String organizationName;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Address is required")
    private String address;

    private String gstInId;
}
