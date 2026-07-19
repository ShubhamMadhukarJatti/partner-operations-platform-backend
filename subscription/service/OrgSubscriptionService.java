package com.sharkdom.subscription.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.reseller.entity.PaymentStatus;
import com.sharkdom.subscription.entity.OrganizationSubscription;
import com.sharkdom.subscription.model.InvoiceResponseDTO;
import com.sharkdom.subscription.model.SubscriptionSummaryResponse;
import com.sharkdom.subscription.repository.OrganizationSubscriptionRepository;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceCollection;
import com.stripe.param.InvoiceListParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrgSubscriptionService {

    private final OrganizationSubscriptionRepository subscriptionRepository;

    public SubscriptionSummaryResponse getSubscriptionSummary(Long orgId) {

        log.info("[SUBSCRIPTION SUMMARY] Fetching for orgId={}", orgId);

        OrganizationSubscription sub = subscriptionRepository
                .findByOrganizationId(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH42, orgId));

        Long daysRemaining = 0L;
        boolean inGrace = false;
        LocalDateTime graceEnd = null;

        if (sub.getPaymentStatus() == PaymentStatus.GRACE_PERIOD) {

            inGrace = true;
            graceEnd = sub.getGracePeriodEnd();

            if (graceEnd != null) {
                daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), graceEnd);
                daysRemaining = Math.max(daysRemaining, 0);
            }

        } else if (sub.getPaymentStatus() == PaymentStatus.ACTIVE) {

        }

        SubscriptionSummaryResponse response = SubscriptionSummaryResponse.builder()
                .status(sub.getPaymentStatus())
                .daysRemaining(daysRemaining)
                .inGrace(inGrace)
                .graceEndsAt(graceEnd)
                .activeSuites(sub.getActiveSuites())
                .build();

        log.info("[SUBSCRIPTION SUMMARY RESULT] orgId={} | status={} | daysRemaining={}",
                orgId, response.getStatus(), response.getDaysRemaining());

        return response;
    }

    public List<InvoiceResponseDTO> getInvoices(Long orgId) {

        log.info("[FETCH INVOICES] orgId={}", orgId);

        OrganizationSubscription sub = subscriptionRepository
                .findByOrganizationId(orgId)
                .orElseThrow(() -> new ServiceException(ErrorMessages.SH42, orgId));

        try {

            InvoiceListParams params = InvoiceListParams.builder()
                    .setSubscription(sub.getStripeSubscriptionId())
                    .setLimit(10L)
                    .build();

            InvoiceCollection invoices = Invoice.list(params);

            List<InvoiceResponseDTO> response = invoices.getData().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());

            log.info("[INVOICES FETCHED] orgId={} count={}", orgId, response.size());

            return response;

        } catch (Exception e) {
            log.error("[INVOICE ERROR] orgId={} error={}", orgId, e.getMessage(), e);
            throw new ServiceException(ErrorMessages.SH116, "Failed to fetch invoices");
        }
    }

    private InvoiceResponseDTO mapToDto(Invoice invoice) {

        return InvoiceResponseDTO.builder()
                .invoiceId(invoice.getId())
                .amount(invoice.getAmountPaid())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus())
                .createdAt(convertToLocalDateTime(invoice.getCreated()))
                .hostedInvoiceUrl(invoice.getHostedInvoiceUrl())
                .invoicePdf(invoice.getInvoicePdf())
                .build();
    }

    private LocalDateTime convertToLocalDateTime(Long epoch) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(epoch),
                ZoneId.systemDefault()
        );
    }
}