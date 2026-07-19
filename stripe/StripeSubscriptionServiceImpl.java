package com.sharkdom.service.stripe;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.stripe.StripeMode;
import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.constants.stripe.StripeSubscriptionStatus;
import com.sharkdom.entity.credits.Credits;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.stripe.*;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.mapper.stripe.StripeSubscriptionMapper;
import com.sharkdom.model.stripe.CreateSubscriptionRequest;
import com.sharkdom.model.stripe.StripeSubscriptionDataDto;
import com.sharkdom.model.stripe.UpgradeResponseDTO;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.stripe.StripeCustomerRepository;
import com.sharkdom.repository.stripe.StripePlanConfigurationRepository;
import com.sharkdom.repository.stripe.StripeSubscriptionRepository;
import com.sharkdom.repository.stripe.UpgradeRecordRepository;
import com.sharkdom.reseller.entity.PaymentStatus;
import com.sharkdom.subscription.entity.*;
import com.sharkdom.subscription.entity.Product;
import com.sharkdom.subscription.repository.OrganizationSubscriptionRepository;
import com.sharkdom.subscription.repository.OrganizationSuiteRepository;
import com.sharkdom.subscription.repository.ProductRepository;
import com.sharkdom.util.Util;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import com.stripe.param.SubscriptionCreateParams;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeSubscriptionServiceImpl implements StripeSubscriptionService {

    private final StripeSubscriptionRepository stripeSubscriptionRepository;

    private final StripeSubscriptionMapper stripeSubscriptionMapper;

    private final OrganizationRepository organizationRepository;

    private final StripePlanConfigurationRepository stripePlanConfigurationRepository;

    private final StripeEmailService stripeEmailService;

    private final StripePlanConfigurationService stripePlanConfigurationService;

    private final StripeCustomerRepository stripeCustomerRepository;

    private final StripeWebhookQueryService stripeWebhookQueryService;

    private final UpgradeRecordRepository upgradeRepo;

    private static final String CANCEL_MESSAGE = "canceled";

    private final OrganizationSubscriptionRepository orgSubRepo;
    private final OrganizationSuiteRepository orgSuiteRepo;
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public StripeSubscriptionDataDto getSubscriptionBySubscriptionId(String subscriptionId) {
        StripeSubscriptionData bySubscriptionId = stripeSubscriptionRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH55, "retrieving", subscriptionId));
        return stripeSubscriptionMapper.stripeSubscriptionDataToStripeSubscriptionDataDto(bySubscriptionId);
    }

    @Transactional
    @Override
    public List<StripeSubscriptionDataDto> getSubscriptionsByOrganizationId(Long organizationId) {
        List<StripeSubscriptionData> byOrganizationId = stripeSubscriptionRepository
                .findByOrganizationId(organizationId);
        return stripeSubscriptionMapper.stripeSubscriptionDataListToStripeSubscriptionDataDtoList(byOrganizationId);
    }

    @Override
    @Transactional
    public StripeSubscriptionDataDto cancelSubscription(String subscriptionId, boolean requestRefund)
            throws StripeException {
        try {
            StripeSubscriptionData currentSubscription = stripeSubscriptionRepository
                    .findBySubscriptionId(subscriptionId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorMessages.SH55, "upgradation", subscriptionId));

            // Retrieve subscription from Stripe
            Subscription stripeSubscription = Subscription.retrieve(currentSubscription.getSubscriptionId());

            Subscription cancelSubscription;
            // Process refund if requested and not in trial
            if (requestRefund && !currentSubscription.getStatus().equals(StripeSubscriptionStatus.TRIALING)) {
                cancelSubscription = processRefund(stripeSubscription);
                log.info("Refund process completed");
            } else {
                // Cancel the subscription only no refund
                cancelSubscription = stripeSubscription.cancel();
            }

            // Update local subscription
            StripeSubscriptionData mappedStripeSubscriptionDataFromStripeSubscription = stripeWebhookQueryService
                    .getStripeSubscriptionData(cancelSubscription);

            StripeSubscriptionData savedCancelSubscriptionData = stripeSubscriptionRepository
                    .save(mappedStripeSubscriptionDataFromStripeSubscription);
            log.info("Saved Cancel SubscriptionData: {}", savedCancelSubscriptionData);

            stripeEmailService.sendConfirmationEmailAndNotification(savedCancelSubscriptionData, CANCEL_MESSAGE,
                    currentSubscription);
            log.info("Email and notification send for the subscription canceled");
            updateCreditsToOrganizations(savedCancelSubscriptionData);

            return stripeSubscriptionMapper
                    .stripeSubscriptionDataToStripeSubscriptionDataDto(savedCancelSubscriptionData);
        } catch (StripeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Something went wrong while cancel subscription: {}", e.getMessage());
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

    @Override
    @Transactional
    public UpgradeResponseDTO upgradeSubscription(String subscriptionId, StripePlanType planType, String successUrl,
                                                  String cancelUrl) throws StripeException {
        String newPriceId = stripePlanConfigurationService.getPriceIdByPlanType(planType);
        Subscription currentSub = Subscription.retrieve(subscriptionId);
        String currentPriceId = currentSub.getItems().getData().get(0).getPrice().getId();
        return processUpgradeSubscription(subscriptionId, successUrl, cancelUrl, currentSub, newPriceId, currentPriceId,
                "upgrade");
    }

    @NotNull
    private UpgradeResponseDTO processUpgradeSubscription(String subscriptionId, String successUrl, String cancelUrl,
                                                          Subscription currentSub, String newPriceId, String currentPriceId, String action) throws StripeException {

        // 2. Calculate prorated amount
        Invoice preview = calculateProratedAmount(currentSub, newPriceId);

        long dueAmount = getAmountDueForCurrentSubscription(preview);
        log.info("Calculated Due Amount for upgradeDowngradeSubscription {} ", dueAmount / 100);
        // 3. Create checkout session
        assert newPriceId != null;
        Session session;
        if (action.equals("upgrade") && dueAmount > 0) {
            session = createCheckoutSessionUpgradeDowngradeSubscription(
                    currentSub,
                    dueAmount,
                    preview.getCurrency(),
                    successUrl,
                    cancelUrl,
                    Map.of(
                            "subscription_id", subscriptionId,
                            "new_price_id", newPriceId,
                            "action", action));
        } else if (action.equals("upgradeSeat") && dueAmount > 0) {
            session = createCheckoutSessionForSeatUpgradeSubscription(
                    currentSub,
                    dueAmount,
                    preview.getCurrency(),
                    successUrl,
                    cancelUrl,
                    Map.of(
                            "subscription_id", subscriptionId,
                            "new_price_id", newPriceId,
                            "action", action));
        } else {
            return new UpgradeResponseDTO("payment_fail",
                    "Due Amount is smaller than zero so no session will be created for checkout. Due Amount : "
                            + dueAmount / 100
                            + "\nPlease use downgrade api to credit the due amount " + (dueAmount * -1) / 100);
        }

        // 4. Store upgrade record
        StripeSubscriptionUpgradeRecord upgradeRecord = new StripeSubscriptionUpgradeRecord();
        upgradeRecord.setSubscriptionId(subscriptionId);
        upgradeRecord.setCurrentPriceId(currentPriceId);
        upgradeRecord.setNewPriceId(newPriceId);
        upgradeRecord.setCheckoutSessionId(session.getId());
        upgradeRecord.setStatus("pending");
        upgradeRecord.setCreatedAt(Instant.now());
        upgradeRepo.save(upgradeRecord);

        String paymentUrl = session.getUrl();

        return new UpgradeResponseDTO(
                "requires_payment",
                paymentUrl,
                (double) dueAmount / 100,
                preview.getCurrency(),
                upgradeRecord.getId(),
                null);
    }

    @Transactional
    @Override
    public Map<String, Object> downgradeSubscriptionV1(String subscriptionId, StripePlanType planType, String successUrl,
                                                     String cancelUrl) throws StripeException {
        String newPriceId = stripePlanConfigurationService.getPriceIdByPlanType(planType);
        Subscription currentSub = Subscription.retrieve(subscriptionId);
        String customerId = currentSub.getCustomer();
        return processDowngradeSubscription(subscriptionId, customerId, currentSub, newPriceId);
    }

    private Map<String, Object> processDowngradeSubscription(String subscriptionId, String customerId,
                                                             Subscription currentSub, String newPriceId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 1. Calculate proration (will be negative for downgrades)
            Invoice preview = calculateProratedAmount(currentSub, newPriceId);

            long prorationAmount = getAmountDueForCurrentSubscription(preview);

            if (prorationAmount >= 0) {
                throw new ServiceException(ErrorMessages.SH140);
            }

            // 2. Convert negative amount to positive credit
            long creditAmount = prorationAmount * -1;

            // 3. Create customer balance transaction (credit)
            CustomerBalanceTransaction credit = createCustomerCredit(customerId, creditAmount, preview.getCurrency());

            // 4. Update subscription (no immediate charge)
            Subscription subscription = updateSubscription(subscriptionId, newPriceId);

            // 5. Prepare response
            response.put("status", "success");
            response.put("credit_amount", creditAmount);
            response.put("credit_currency", "inr");
            response.put("subscription_id", subscription.getId());
            response.put("new_price_id", newPriceId);
            response.put("balance_transaction_id", credit.getId());

        } catch (StripeException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    private CustomerBalanceTransaction createCustomerCredit(String customerId, long amount, String currency)
            throws StripeException {
        return Customer.retrieve(customerId)
                .balanceTransactions().create(CustomerBalanceTransactionCollectionCreateParams.builder()
                        .setAmount(amount)
                        .setCurrency(currency)
                        .setDescription("downgrade")
                        .build());
    }

    private Subscription updateSubscription(String subscriptionId, String newPriceId)
            throws StripeException {

        return Subscription.retrieve(subscriptionId)
                .update(
                        SubscriptionUpdateParams.builder()
                                .addItem(
                                        SubscriptionUpdateParams.Item.builder()
                                                .setId(Subscription.retrieve(subscriptionId)
                                                        .getItems().getData().get(0).getId())
                                                .setPrice(newPriceId)
                                                .build())
                                .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.NONE)
                                .build());
    }

    @Override
    @Transactional
    public UpgradeResponseDTO upgradeSeat(String subscriptionId, StripePlanType planType, String successUrl,
                                          String cancelUrl) throws StripeException {
        String newPriceId = stripePlanConfigurationService.getPriceIdByPlanType(planType);
        Subscription currentSub = Subscription.retrieve(subscriptionId);
        String currentPriceId = currentSub.getItems().getData().get(0).getPrice().getId();
        return processUpgradeSubscription(subscriptionId, successUrl, cancelUrl, currentSub, newPriceId, currentPriceId,
                "upgradeSeat");

    }

    @Override
    @Transactional
    public Map<String, Object> downgradeSeat(String subscriptionId, StripePlanType planType, String successUrl,
                                             String cancelUrl) throws StripeException {
        String newPriceId = stripePlanConfigurationService.getPriceIdByPlanType(planType);
        Subscription currentSub = Subscription.retrieve(subscriptionId);
        String customerId = currentSub.getCustomer();
        return processDowngradeSubscription(subscriptionId, customerId, currentSub, newPriceId);
    }

    public Session createCheckoutSessionUpgradeDowngradeSubscription(Subscription subscription, Long amount,
                                                                     String currency, String successUrl, String cancelUrl, Map<String, String> metadata) throws StripeException {
        Session session = getSession(subscription, amount, currency, successUrl, cancelUrl, metadata);
        log.info("Seat upgradation session created in stripe");
        StripeCheckoutSessions savedStripeCheckoutSession = getSavedStripeCheckoutSession(successUrl, cancelUrl,
                metadata, session);
        log.info("Created checkout session and saved the checkout session for seat upgrade {}",
                savedStripeCheckoutSession);
        return session;
    }

    public Session createCheckoutSessionForSeatUpgradeSubscription(Subscription subscription, Long amount,
                                                                   String currency, String successUrl, String cancelUrl, Map<String, String> metadata) throws StripeException {
        Session session = getSession(subscription, amount, currency, successUrl, cancelUrl, metadata);
        log.info("initiate upgradation session created in stripe");
        StripeCheckoutSessions savedStripeCheckoutSession = getSavedStripeCheckoutSession(successUrl, cancelUrl,
                metadata, session);
        log.info("Created checkout session and saved the checkout session for subscription upgrade {}",
                savedStripeCheckoutSession);
        return session;
    }

    private StripeCheckoutSessions getSavedStripeCheckoutSession(String successUrl, String cancelUrl,
                                                                 Map<String, String> metadata, Session session) {
        StripePlanType planType = null;
        try {
            planType = stripePlanConfigurationService.getPlanTypeByPriceId(metadata.get("new_price_id"));
        } catch (ResourceNotFoundException e) {
            log.warn("No plan type found for dynamic priceId: {}", metadata.get("new_price_id"));
        }
        return stripeWebhookQueryService.getSavedStripeCheckoutSession(StripeCheckoutSessions.builder()
                .sessionId(session.getId())
                .customer(stripeCustomerRepository.findByCustomerId(session.getCustomer()).orElse(null))
                .mode(StripeMode.valueOf(session.getMode().toUpperCase()))
                .lineItems(List.of(
                        LineItemEntity.builder()
                                .quantity(1L)
                                .price(PriceEntity.builder()
                                        .stripePriceId(metadata.get("new_price_id"))
                                        .planType(planType)
                                        .build())
                                .build()))
                .cancelUrl(cancelUrl)
                .successUrl(successUrl)
                .paymentMethodTypes(List.of("card"))
                .build());
    }

    private static Session getSession(Subscription subscription, Long amount, String currency, String successUrl,
                                      String cancelUrl, Map<String, String> metadata) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomer(subscription.getCustomer())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Plan Upgrade Proration")
                                                                .build())
                                                .build())
                                .build())
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putAllMetadata(metadata)
                // .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                // .setDescription("Plan upgrade proration charges")
                // .build())
                .build();
        return Session.create(params);
    }

    public Invoice calculateProratedAmount(Subscription subscription, String newPriceId) throws StripeException {
        return Invoice.upcoming(InvoiceUpcomingParams.builder()
                .setCustomer(subscription.getCustomer())
                .setSubscription(subscription.getId())
                .addSubscriptionItem(InvoiceUpcomingParams.SubscriptionItem.builder()
                        .setId(subscription.getItems().getData().get(0).getId())
                        .setPrice(newPriceId)
                        .build())
                .build());
    }

    private static long getAmountDueForCurrentSubscription(Invoice invoice) {
        return invoice.getLines().getData().stream()
                .filter(line -> line.getProration() != null && line.getProration())
                .mapToLong(InvoiceLineItem::getAmount)
                .sum();
    }

    private Subscription processRefund(Subscription stripeSubscription) throws StripeException {

        String subscriptionId = stripeSubscription.getId();

        SubscriptionRetrieveParams subRetrieveParams = SubscriptionRetrieveParams.builder()
                .addExpand("latest_invoice") // Expand the latest invoice object
                .addExpand("latest_invoice.payment_intent") // Also expand the payment intent within the invoice
                .build();
        Subscription subscription = Subscription.retrieve(stripeSubscription.getId(), subRetrieveParams, null);

        if (ObjectUtils.isEmpty(subscription)) {
            throw new ResourceNotFoundException(ErrorMessages.SH55, "retrieving", subscriptionId);
        }

        // Check if already canceled
        if ("canceled".equals(subscription.getStatus())) {
            log.warn("Subscription {} is already canceled.", subscriptionId);
            // Decide if you want to throw an error or proceed to check for refund
            // For this example, we'll stop here if already canceled.
            throw new ServiceException(ErrorMessages.SH139, subscriptionId);
        }

        Invoice latestInvoice = subscription.getLatestInvoiceObject();
        String chargeId = null;
        String paymentIntentId = null;
        long refundAmount = 0;

        // 2. Find the Charge or Payment Intent ID from the Latest Invoice
        if (latestInvoice != null) {
            // Prefer PaymentIntent ID if available (newer API)
            if (latestInvoice.getPaymentIntentObject() != null) {
                paymentIntentId = latestInvoice.getPaymentIntentObject().getId();
                refundAmount = extractedRefundAmount(latestInvoice);
                log.info("Found latest PaymentIntent ID: {}", paymentIntentId);
            } else if (latestInvoice.getCharge() != null) {
                // Fallback to Charge ID
                chargeId = latestInvoice.getCharge();
                refundAmount = extractedRefundAmount(latestInvoice);
                log.info("Found latest Charge ID: {}", chargeId);
            } else {
                log.warn(
                        "Subscription {} has a latest invoice ({}) but no associated Charge or Payment Intent ID. Cannot refund.",
                        subscriptionId, latestInvoice.getId());
                // No refundable payment found on the last invoice
            }
            // Ensure the invoice was actually paid successfully before refunding
            if (!"paid".equalsIgnoreCase(latestInvoice.getStatus())) {
                log.warn("Latest invoice {} for subscription {} is not in 'paid' status (status: {}). Cannot refund.",
                        latestInvoice.getId(), subscriptionId, latestInvoice.getStatus());
                paymentIntentId = null; // Reset IDs if invoice wasn't paid
                chargeId = null;
            }
        } else {
            log.warn("Subscription {} has no latest invoice. Cannot refund.", subscriptionId);
            // No invoice found, cannot refund
        }

        // 3. Cancel the Subscription immediately
        // No parameters needed for immediate cancellation by default
        Subscription canceledSubscription = subscription.cancel();
        log.info("Successfully canceled subscription: {}. Status: {}", canceledSubscription.getId(),
                canceledSubscription.getStatus());
        // 4. Create the Refund (if a chargeable ID was found)
        Refund refund;
        if (paymentIntentId != null) {
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER) // Optional reason
                    .setAmount(refundAmount) // Not setting 'amount' refunds the full amount
                    .build();
            refund = Refund.create(refundParams);
            log.info("Successfully created refund {} for PaymentIntent {}", refund.getId(), paymentIntentId);
        } else if (chargeId != null) {
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setCharge(chargeId)
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER) // Optional reason
                    .setAmount(refundAmount)// Not setting 'amount' refunds the full amount
                    .build();
            refund = Refund.create(refundParams);
            log.info("Successfully created refund {} for Charge {}", refund.getId(), chargeId);
        } else {
            log.warn(
                    "No refundable Charge or PaymentIntent found for the last invoice of subscription {}. Cancellation done, but no refund issued.",
                    subscriptionId);
        }
        return canceledSubscription;
    }

    private long extractedRefundAmount(Invoice latestInvoice) {
        long daysUsed = ChronoUnit.DAYS.between(
                Instant.ofEpochSecond(latestInvoice.getPeriodStart()).atZone(ZoneId.systemDefault()).toLocalDate(),
                LocalDate.now());

        long totalDays = ChronoUnit.DAYS.between(
                Instant.ofEpochSecond(latestInvoice.getPeriodStart()).atZone(ZoneId.systemDefault()).toLocalDate(),
                Instant.ofEpochSecond(latestInvoice.getPeriodEnd()).atZone(ZoneId.systemDefault()).toLocalDate());

        return (latestInvoice.getAmountPaid() * (totalDays - daysUsed) / totalDays);
    }

    private void updateCreditsToOrganizations(StripeSubscriptionData stripeSubscriptionData) {
        Set<Long> organizationIds = stripeSubscriptionData.getOrganizationId();
        StripePlanType stripePlanType = stripeSubscriptionData.getPrice().getPlanType();
        if (!ObjectUtils.isEmpty(organizationIds)) {
            organizationIds.forEach(organizationId -> {
                Organization organization = organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH72,
                                stripeSubscriptionData.getSubscriptionId(), organizationId));

                organization.setSubscribed(!stripeSubscriptionData.getStatus().equals(StripeSubscriptionStatus.CANCELED)
                        &&
                        !stripeSubscriptionData.getStatus().equals(StripeSubscriptionStatus.INCOMPLETE) &&
                        !stripeSubscriptionData.getStatus().equals(StripeSubscriptionStatus.INCOMPLETE_EXPIRED));

                Credits credits = organization.getCredits();
                if (credits == null) {
                    credits = new Credits();
                }
                Credits updatedCredits = resetCreditsFromPlanAfterSubscriptionUpdate(stripeSubscriptionData.getStatus(),
                        stripePlanType, credits);
                log.info("Updated Credits for Organization after subscription updated: {}", updatedCredits);
                organization.setCredits(updatedCredits);
                Organization updatedOrganization = organizationRepository.save(organization);
                log.info("Updated Organization after subscription updated: {}", updatedOrganization);
            });
        }
    }

    private Credits resetCreditsFromPlanAfterSubscriptionUpdate(StripeSubscriptionStatus status,
                                                                StripePlanType stripePlanType, Credits credits) {
        if (status.equals(StripeSubscriptionStatus.CANCELED) ||
                status.equals(StripeSubscriptionStatus.INCOMPLETE) ||
                status.equals(StripeSubscriptionStatus.INCOMPLETE_EXPIRED)) {
            int aiProposalCredits = 1;
            int playgroundCredits = 3;
            int collaborationSent = 4;
            credits.setAiProposalAllocated(aiProposalCredits);
            credits.setPlaygroundAllocated(playgroundCredits);
            credits.setCollaborationsAllocated(collaborationSent);
            return credits;
        }
        StripePlanConfiguration stripePlanConfiguration = stripePlanConfigurationRepository.findById(stripePlanType)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH53, stripePlanType,
                        "So credits will not be updated"));
        int aiProposalCredits = 1;
        int playgroundCredits = 3;
        int collaborationSent = 4;
        aiProposalCredits += stripePlanConfiguration.getAiProposalCredits();
        playgroundCredits += stripePlanConfiguration.getPlaygroundCredits();
        collaborationSent += stripePlanConfiguration.getCollaborationSent();
        credits.setAiProposalAllocated(aiProposalCredits);
        credits.setPlaygroundAllocated(playgroundCredits);
        credits.setCollaborationsAllocated(collaborationSent);
        return credits;
    }

//    @Override
//    @Transactional
//    public StripeSubscriptionDataDto createSubscription(CreateSubscriptionRequest request) throws StripeException {
//
//        String customerId = request.getCustomerId();
//        List<String> productIds = request.getProductIds();
//        String intervalStr = request.getInterval();
//        Long amount = request.getAmount();
//        String currency = request.getCurrency();
//        Long trialPeriodDays = request.getTrialPeriodDays();
//
//        // 1. Validate productIds
//        if (productIds == null || productIds.isEmpty()) {
//            throw new ServiceException(ErrorMessages.SH116, "At least one productId must be provided");
//        }
//
//        // 2. Validate customer exists in DB
//        stripeCustomerRepository.findByCustomerId(customerId)
//                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH44, customerId));
//
//        // 3. Derive Stripe interval from request
//        PriceCreateParams.Recurring.Interval interval;
//        if ("MONTHLY".equalsIgnoreCase(intervalStr)) {
//            interval = PriceCreateParams.Recurring.Interval.MONTH;
//        } else if ("YEARLY".equalsIgnoreCase(intervalStr)) {
//            interval = PriceCreateParams.Recurring.Interval.YEAR;
//        } else {
//            throw new ServiceException(ErrorMessages.SH116,
//                    "Invalid interval: " + intervalStr + ". Must be MONTHLY or YEARLY.");
//        }
//
//        // 4. Distribute amount equally across products (remainder goes to first)
//        long perProductAmount = amount / productIds.size();
//        long remainder = amount % productIds.size();
//
//        // 5. Create a dynamic Stripe Price for each product
//        List<SubscriptionCreateParams.Item> items = new ArrayList<>();
//        for (int i = 0; i < productIds.size(); i++) {
//            long itemAmount = perProductAmount + (i == 0 ? remainder : 0);
//
//            PriceCreateParams priceParams = PriceCreateParams.builder()
//                    .setCurrency(currency)
//                    .setUnitAmount(itemAmount)
//                    .setRecurring(PriceCreateParams.Recurring.builder()
//                            .setInterval(interval)
//                            .build())
//                    .setProduct(productIds.get(i))
//                    .build();
//            Price dynamicPrice = Price.create(priceParams);
//            log.info("Dynamic price created: {} for product: {} with amount: {} {}",
//                    dynamicPrice.getId(), productIds.get(i), itemAmount, currency);
//
//            items.add(
//                    SubscriptionCreateParams.Item.builder()
//                            .setPrice(dynamicPrice.getId())
//                            .build()
//            );
//        }
//
//        // 6. Get default payment method from Stripe customer
//        Customer customer = Customer.retrieve(customerId);
//        String paymentMethodId = customer.getInvoiceSettings().getDefaultPaymentMethod();
//
//        if (paymentMethodId == null || paymentMethodId.isBlank()) {
//            throw new ServiceException(ErrorMessages.SH44,
//                    "No default payment method found for customer: " + customerId);
//        }
//
//        // 7. Build subscription params with all line items
//        SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
//                .setCustomer(customerId)
//                .addAllItem(items)
//                .setDefaultPaymentMethod(paymentMethodId);
//
//        if (trialPeriodDays != null && trialPeriodDays > 0) {
//            paramsBuilder.setTrialPeriodDays(trialPeriodDays);
//        }
//
//        // 8. Call Stripe API to create subscription
//        Subscription subscription = Subscription.create(paramsBuilder.build());
//        log.info("Subscription created: {} for customer: {} with status: {} and {} line items",
//                subscription.getId(), customerId, subscription.getStatus(), productIds.size());
//
//        // 9. Map + save to DB
//        StripeSubscriptionData subscriptionData = stripeWebhookQueryService.getStripeSubscriptionData(subscription);
//        StripeSubscriptionData saved = stripeSubscriptionRepository.save(subscriptionData);
//
//        return stripeSubscriptionMapper.stripeSubscriptionDataToStripeSubscriptionDataDto(saved);
//    }

//    @Override
//    @Transactional
//    public StripeSubscriptionDataDto createSubscription(CreateSubscriptionRequest request) throws StripeException {
//
//        String customerId = request.getCustomerId();
//        List<String> productIds = request.getProductIds();
//        String intervalStr = request.getInterval();
//        Long amount = request.getAmount();
//        String currency = request.getCurrency();
//        Long trialPeriodDays = request.getTrialPeriodDays();
//        Long orgId = Util.getOrgIdFromToken();
//
//        // 1. Validate productIds
//        if (productIds == null || productIds.isEmpty()) {
//            throw new ServiceException(ErrorMessages.SH116, "At least one productId must be provided");
//        }
//
//        // 2. Validate customer exists
//        stripeCustomerRepository.findByCustomerId(customerId)
//                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH44, customerId));
//
//        // 3. Interval mapping
//        PriceCreateParams.Recurring.Interval interval;
//        if ("MONTHLY".equalsIgnoreCase(intervalStr)) {
//            interval = PriceCreateParams.Recurring.Interval.MONTH;
//        } else if ("YEARLY".equalsIgnoreCase(intervalStr)) {
//            interval = PriceCreateParams.Recurring.Interval.YEAR;
//        } else {
//            throw new ServiceException(ErrorMessages.SH116, "Invalid interval");
//        }
//
//        // 4. Amount split
//        long perProductAmount = amount / productIds.size();
//        long remainder = amount % productIds.size();
//
//        List<SubscriptionCreateParams.Item> items = new ArrayList<>();
//
//        for (int i = 0; i < productIds.size(); i++) {
//
//            long itemAmount = perProductAmount + (i == 0 ? remainder : 0);
//
//            Price price = Price.create(
//                    PriceCreateParams.builder()
//                            .setCurrency(currency)
//                            .setUnitAmount(itemAmount)
//                            .setRecurring(
//                                    PriceCreateParams.Recurring.builder()
//                                            .setInterval(interval)
//                                            .build()
//                            )
//                            .setProduct(productIds.get(i))
//                            .build()
//            );
//
//            items.add(
//                    SubscriptionCreateParams.Item.builder()
//                            .setPrice(price.getId())
//                            .build()
//            );
//        }
//
//        // 5. Default payment method
//        Customer customer = Customer.retrieve(customerId);
//        String paymentMethodId = customer.getInvoiceSettings().getDefaultPaymentMethod();
//
//        if (paymentMethodId == null || paymentMethodId.isBlank()) {
//            throw new ServiceException(ErrorMessages.SH44,
//                    "No default payment method found for customer");
//        }
//
//        // 6. Create subscription
//        SubscriptionCreateParams.Builder paramsBuilder =
//                SubscriptionCreateParams.builder()
//                        .setCustomer(customerId)
//                        .addAllItem(items)
//                        .setDefaultPaymentMethod(paymentMethodId);
//
//        if (trialPeriodDays != null && trialPeriodDays > 0) {
//            paramsBuilder.setTrialPeriodDays(trialPeriodDays);
//        }
//
//        Subscription subscription = Subscription.create(paramsBuilder.build());
//
//        log.info("Subscription created: {}", subscription.getId());
//
//        // ===================== BUSINESS LOGIC =====================
//
//        // 1. Save Organization Subscription
//        OrganizationSubscription orgSub = new OrganizationSubscription();
//        orgSub.setOrganizationId(orgId);
//        orgSub.setStripeSubscriptionId(subscription.getId());
//        orgSub.setPaymentStatus(PaymentStatus.ACTIVE);
//
//        orgSubRepo.save(orgSub);
//
//        // 2. Map products → suites
//        List<SuiteKey> suites = mapProductsToSuites(productIds);
//
//        // 3. Save suites
//        for (SuiteKey suite : suites) {
//            OrganizationSuite orgSuite = new OrganizationSuite();
//            orgSuite.setOrganizationId(orgId);
//            orgSuite.setSuiteKey(suite);
//            orgSuite.setActive(true);
//
//            orgSuiteRepo.save(orgSuite);
//        }
//
//        // ===================== EXISTING LOGIC =====================
//
//        StripeSubscriptionData subscriptionData =
//                stripeWebhookQueryService.getStripeSubscriptionData(subscription);
//
//        StripeSubscriptionData saved =
//                stripeSubscriptionRepository.save(subscriptionData);
//
//        return stripeSubscriptionMapper
//                .stripeSubscriptionDataToStripeSubscriptionDataDto(saved);
//    }

    // ===================== PRODUCT → SUITE MAPPING =====================

    private List<SuiteKey> mapProductsToSuites(List<String> productIds) {

        var products = productRepository.findByProductIdIn(productIds);

        List<SuiteKey> suites = new ArrayList<>();

        for (Product product : products) {

            ModuleName module = product.getProductName();

            SuiteKey suiteKey = SuiteKey.valueOf(module.name());

            suites.add(suiteKey);
        }

        return suites;
    }

    public void downgradeSuites(Long orgId, List<SuiteKey> suitesToDisable) {

        List<OrganizationSuite> suites =
                orgSuiteRepo.findByOrganizationIdAndActiveTrue(orgId);

        for (OrganizationSuite suite : suites) {
            if (suitesToDisable.contains(suite.getSuiteKey())) {
                suite.setActive(false);
            }
        }

        orgSuiteRepo.saveAll(suites);
    }

    private void handleCancel(Event event) {

        com.stripe.model.Subscription stripeSub =
                (com.stripe.model.Subscription) event.getDataObjectDeserializer().getObject().get();

        OrganizationSubscription sub = orgSubRepo
                .findByStripeSubscriptionId(stripeSub.getId())
                .orElseThrow();

        sub.setPaymentStatus(PaymentStatus.FREE);

        orgSubRepo.save(sub);
    }

    private void handlePaymentSuccess(Event event) {

        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().get();

        OrganizationSubscription sub = orgSubRepo
                .findByStripeSubscriptionId(invoice.getSubscription())
                .orElseThrow();

        sub.setPaymentStatus(PaymentStatus.ACTIVE);
        sub.setGracePeriodEnd(null);

        orgSubRepo.save(sub);
    }

    private void handlePaymentFailed(Event event) {

        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().get();

        OrganizationSubscription sub = orgSubRepo
                .findByStripeSubscriptionId(invoice.getSubscription())
                .orElseThrow();

        sub.setPaymentStatus(PaymentStatus.GRACE_PERIOD);
        sub.setGracePeriodEnd(LocalDateTime.now().plusDays(7));

        orgSubRepo.save(sub);
    }

    @Override
    @Transactional
    public StripeSubscriptionDataDto createSubscription(CreateSubscriptionRequest request) throws StripeException {

        Long orgId = Util.getOrgIdFromToken();

        log.info("[CREATE SUBSCRIPTION START] orgId={} | customerId={} | products={}",
                orgId, request.getCustomerId(), request.getProductIds());

        String customerId = request.getCustomerId();
        List<String> productIds = request.getProductIds();
        String intervalStr = request.getInterval();
        Long amount = request.getAmount();
        String currency = request.getCurrency();
        Long trialPeriodDays = request.getTrialPeriodDays();

        // ===================== VALIDATIONS =====================

        if (productIds == null || productIds.isEmpty()) {
            log.error("[VALIDATION FAILED] No productIds provided");
            throw new ServiceException(ErrorMessages.SH116, "At least one productId must be provided");
        }

        stripeCustomerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> {
                    log.error("[VALIDATION FAILED] Customer not found customerId={}", customerId);
                    return new ResourceNotFoundException(ErrorMessages.SH44, customerId);
                });

        // ===================== INTERVAL =====================

        PriceCreateParams.Recurring.Interval interval;

        if ("MONTHLY".equalsIgnoreCase(intervalStr)) {
            interval = PriceCreateParams.Recurring.Interval.MONTH;
        } else if ("YEARLY".equalsIgnoreCase(intervalStr)) {
            interval = PriceCreateParams.Recurring.Interval.YEAR;
        } else {
            log.error("[VALIDATION FAILED] Invalid interval={}", intervalStr);
            throw new ServiceException(ErrorMessages.SH116, "Invalid interval");
        }

        // ===================== PRICE CREATION =====================

        long perProductAmount = amount / productIds.size();
        long remainder = amount % productIds.size();

        List<SubscriptionCreateParams.Item> items = new ArrayList<>();

        for (int i = 0; i < productIds.size(); i++) {

            long itemAmount = perProductAmount + (i == 0 ? remainder : 0);

            Price price = Price.create(
                    PriceCreateParams.builder()
                            .setCurrency(currency)
                            .setUnitAmount(itemAmount)
                            .setRecurring(
                                    PriceCreateParams.Recurring.builder()
                                            .setInterval(interval)
                                            .build()
                            )
                            .setProduct(productIds.get(i))
                            .build()
            );

            log.info("[PRICE CREATED] productId={} | priceId={} | amount={}",
                    productIds.get(i), price.getId(), itemAmount);

            items.add(
                    SubscriptionCreateParams.Item.builder()
                            .setPrice(price.getId())
                            .build()
            );
        }

        // ===================== PAYMENT METHOD =====================

        Customer customer = Customer.retrieve(customerId);
        String paymentMethodId = customer.getInvoiceSettings().getDefaultPaymentMethod();

        if (paymentMethodId == null || paymentMethodId.isBlank()) {
            log.error("[PAYMENT ERROR] No default payment method for customerId={}", customerId);
            throw new ServiceException(ErrorMessages.SH113, customerId);
        }

        // ===================== CREATE SUBSCRIPTION =====================

        SubscriptionCreateParams.Builder paramsBuilder =
                SubscriptionCreateParams.builder()
                        .setCustomer(customerId)
                        .addAllItem(items)
                        .setDefaultPaymentMethod(paymentMethodId);

        if (trialPeriodDays != null && trialPeriodDays > 0) {
            paramsBuilder.setTrialPeriodDays(trialPeriodDays);
        }

        Subscription subscription = Subscription.create(paramsBuilder.build());

        log.info("[STRIPE SUB CREATED] subscriptionId={}", subscription.getId());

        // ===================== BUSINESS LOGIC =====================

        // Map products → suites
        List<SuiteKey> suites = mapProductsToSuites(productIds);

        // Save / Update Organization Subscription
        OrganizationSubscription orgSub = orgSubRepo
                .findByOrganizationId(orgId)
                .orElse(new OrganizationSubscription());

        orgSub.setOrganizationId(orgId);
        orgSub.setStripeSubscriptionId(subscription.getId());
        orgSub.setPaymentStatus(PaymentStatus.ACTIVE);

        orgSub.setActiveSuites(suites);

        orgSubRepo.save(orgSub);

        log.info("[ORG SUB SAVED] orgId={} | subscriptionId={} | suites={}",
                orgId, subscription.getId(), suites);

        // REMOVE THIS (duplicate design - now stored in orgSub)
        // OrganizationSuite table optional hai, but not required if using activeSuites

        // ===================== SAVE STRIPE DATA =====================

        StripeSubscriptionData subscriptionData =
                stripeWebhookQueryService.getStripeSubscriptionData(subscription);

        StripeSubscriptionData saved =
                stripeSubscriptionRepository.save(subscriptionData);

        log.info("[SUBSCRIPTION COMPLETE] orgId={} | subscriptionId={}", orgId, subscription.getId());

        return stripeSubscriptionMapper
                .stripeSubscriptionDataToStripeSubscriptionDataDto(saved);
    }



}
