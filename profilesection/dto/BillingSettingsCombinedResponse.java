package com.sharkdom.profilesection.dto;
import com.sharkdom.dto.AddressContactResponse;
import com.sharkdom.subscription.model.InvoiceResponseDTO;
import com.sharkdom.subscription.model.ModuleSubscriptionPlanResponse;
import com.sharkdom.subscription.model.SubscriptionSummaryResponse;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingSettingsCombinedResponse {

    private AddressContactResponse addressContact;

    private ModuleSubscriptionPlanResponse subscriptionPlan;

    private SubscriptionSummaryResponse subscriptionSummary;

    private List<InvoiceResponseDTO> invoices;
}