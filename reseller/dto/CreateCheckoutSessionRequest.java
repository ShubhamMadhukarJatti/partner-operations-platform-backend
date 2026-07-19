package com.sharkdom.reseller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "Request DTO for creating Stripe Checkout Session")
public class CreateCheckoutSessionRequest {

    @Schema(hidden = true)
    private String connectedAccountId; // set internally, never from client


    @Email(message = "Invalid customer email")
    @NotBlank(message = "Customer email is required")
    @Schema(example = "customer@example.com")
    private String customerEmail;


    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    @Schema(example = "Premium Vendor Listing")
    private String productName;


    @NotNull(message = "Unit amount is required")
    @Min(value = 100, message = "Amount must be at least ₹1")
    @Schema(example = "150000", description = "Amount in paise (₹1500 = 150000)")
    private Long unitAmount;


    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[a-zA-Z]{3}$", message = "Currency must be 3-letter ISO code")
    @Schema(example = "inr")
    private String currency;


    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    @Schema(example = "1")
    private Integer quantity;


    @NotBlank(message = "Success URL is required")
    @Schema(example = "https://yourapp.com/payment-success")
    private String successUrl;


    @NotBlank(message = "Cancel URL is required")
    @Schema(example = "https://yourapp.com/payment-cancel")
    private String cancelUrl;


    @NotNull(message = "reseller id is required")
    private Long resellerId;

    @NotNull(message = "deal requested id is required")
    private Long dealRequestedId;

}