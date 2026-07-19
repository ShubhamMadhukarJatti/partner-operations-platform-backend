package com.sharkdom.model.stripe;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeCustomerDto {

    private Long id;

    private String customerId;

    private Set<Long> organizationId;

    private String firebaseUserId;

    @Schema(name = "Customer Name", example = "Rohit Ashish", required = true)
    private String customerName;

    @Email
    @Schema(name = "Customer Email", example = "rohit_ashish@gmail.com", required = true)
    private String customerEmail;

    private String customerPhoneNumber;

    private List<StripeCheckoutSessionsDto> checkoutSessions;

    private List<StripeSubscriptionDataDto> subscriptions;

    private List<StripeCardDetailDto> cardDetails;

}
