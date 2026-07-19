package com.sharkdom.reseller.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.reseller.dto.CreateCheckoutSessionRequest;
import com.sharkdom.reseller.entity.PaymentStatus;
import com.sharkdom.reseller.entity.ResellerPayment;
import com.sharkdom.reseller.repository.ResellerPaymentRepository;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorPaymentService {

    private final StripeCheckoutService stripeCheckoutService;
    private final IntegrationRepository integrationRepository;
    private final ResellerPaymentRepository resellerPaymentRepository;

    public String stripePaymentLinkGeneration(CreateCheckoutSessionRequest request) {

        Long orgId = Util.getOrgIdFromToken();

        log.info("Initiating Stripe payment link generation for organizationId={}", orgId);

        var integration = integrationRepository
                .findByOrganizationIdAndIntegrationType(orgId, IntegrationType.STRIPE);

        if (integration == null) {

            log.error("Stripe integration not found for organizationId={}", orgId);

            throw new ServiceException(
                    ErrorMessages.SH148,
                    "fetching",
                    "Stripe integration",
                    "Integration not found for organizationId: " + orgId
            );
        }

        try {

            request.setConnectedAccountId(integration.getConnectedId());

            var session = stripeCheckoutService.createCheckoutSessionURL(request);

            log.info("Stripe checkout session created successfully for organizationId={}, sessionUrl={}",
                    orgId, session.getUrl());

            var resellerPayment = ResellerPayment.builder()
                    .vendorOrgId(orgId)
                    .paymentStatus(PaymentStatus.PENDING)
                    .resellerId(request.getResellerId())
                    .checkoutUrl(session.getUrl())
                    .customerEmail(request.getCustomerEmail())
                    .amount(request.getUnitAmount() * request.getQuantity())
                    .resellerAccountID(integration.getConnectedId())
                    .requestId(request.getDealRequestedId())
                    .stripeSessionId(session.getId())
                    .build();

            var savedResellerPayment = resellerPaymentRepository.save(resellerPayment);
            log.info("ResellerPayment record created with id={} for organizationId={}",
                    savedResellerPayment.getId(), orgId);

            return session.getUrl();

        } catch (Exception ex) {

            log.error("Error while creating Stripe checkout session for organizationId={}",
                    orgId, ex);

            throw new ServiceException(
                    ErrorMessages.SH150,
                    "creating",
                    ex.getMessage()
            );
        }
    }

    public ResellerPayment getPaymentByRequestIdV1(Long requestId) {

        log.info("Fetching reseller payment details for requestId={}", requestId);

        try {

            var paymentOptional = resellerPaymentRepository
                    .findTopByRequestIdOrderByIdDesc(requestId);

            if (paymentOptional.isEmpty()) {

                log.error("Reseller payment not found for requestId={}", requestId);

                throw new ServiceException(
                        ErrorMessages.SH148,
                        "fetching",
                        "Reseller payment",
                        "Payment not found for requestId: " + requestId
                );
            }

            var payment = paymentOptional.get();

            log.info(
                    "Reseller payment fetched successfully | requestId={}, sessionId={}, status={}",
                    requestId,
                    payment.getStripeSessionId(),
                    payment.getPaymentStatus()
            );

            return payment;

        } catch (ServiceException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error(
                    "Error while fetching reseller payment for requestId={}",
                    requestId,
                    ex
            );

            throw new ServiceException(
                    ErrorMessages.SH150,
                    "fetching",
                    ex.getMessage()
            );
        }
    }

    @Transactional
    public ResellerPayment getPaymentByRequestId(Long requestId) {

        log.info("Fetching reseller payment for requestId={}", requestId);

        try {

            // Step 1: Fetch from DB
            var payment = resellerPaymentRepository
                    .findTopByRequestIdOrderByIdDesc(requestId)
                    .orElseThrow(() ->
                            new ServiceException(
                                    ErrorMessages.SH148,
                                    "fetching",
                                    "Reseller payment",
                                    "Payment not found for requestId: " + requestId
                            )
                    );

            String sessionId = payment.getStripeSessionId();
            String accountId = payment.getResellerAccountID();

            log.info("Calling Stripe API | sessionId={}, accountId={}", sessionId, accountId);

            // Step 2: Call Stripe using your existing method
            String stripeResponse =
                    stripeCheckoutService.getCheckoutSession(sessionId, accountId);

            // Step 3: Parse JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(stripeResponse);

            String stripePaymentStatus =
                    jsonNode.get("payment_status").asText();

            log.info("Stripe payment_status={} for requestId={}",
                    stripePaymentStatus, requestId);

            // Step 4: Update status
            if ("paid".equalsIgnoreCase(stripePaymentStatus)) {

                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                resellerPaymentRepository.save(payment);

            } else {

                payment.setPaymentStatus(PaymentStatus.PENDING);
            }

            // Step 5: Save updated status
            resellerPaymentRepository.save(payment);

            log.info("Payment status updated to {} for requestId={}",
                    payment.getPaymentStatus(),
                    requestId);

            return payment;

        } catch (ServiceException ex) {

            throw ex;

        } catch (Exception ex) {

            log.error("Error verifying payment for requestId={}", requestId, ex);

            throw new ServiceException(
                    ErrorMessages.SH150,
                    "verifying payment",
                    ex.getMessage()
            );
        }
    }
}