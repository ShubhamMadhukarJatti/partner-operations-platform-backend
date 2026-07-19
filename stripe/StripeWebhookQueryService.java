package com.sharkdom.service.stripe;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.stripe.StripeMode;
import com.sharkdom.constants.stripe.StripePaymentStatus;
import com.sharkdom.constants.stripe.StripePlanType;
import com.sharkdom.constants.stripe.StripeSubscriptionStatus;
import com.sharkdom.entity.credits.Credits;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.stripe.*;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.stripe.*;
import com.sharkdom.reseller.entity.PaymentStatus;
import com.sharkdom.subscription.entity.OrganizationSubscription;
import com.sharkdom.subscription.repository.OrganizationSubscriptionRepository;
import com.sharkdom.subscription.repository.OrganizationSuiteRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.SubscriptionUpdateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookQueryService {

    private final StripeSubscriptionRepository stripeSubscriptionRepository;

    private final StripeCheckoutRepository stripeCheckoutRepository;

    private final OrganizationRepository organizationRepository;

    private final StripeEmailService stripeEmailService;

    private final StripePlanConfigurationRepository stripePlanConfigurationRepository;

    private final StripePlanConfigurationService stripePlanConfigurationService;

    private final StripeService stripeService;

    private final StripePaymentRepository stripePaymentRepository;

    private final StripeCustomerRepository stripeCustomerRepository;

    private final UpgradeRecordRepository upgradeRecordRepository;

    private final StripePriceRepository stripePriceRepository;

    private final StripeCouponRepository stripeCouponRepository;

    private final StripeInvoiceRepository stripeInvoiceRepository;

    private final OrganizationSubscriptionRepository orgSubRepo;

    private final OrganizationSuiteRepository suiteRepository;

    protected StripeSubscriptionData getSavedCreatedCustomerSubscription(StripeSubscriptionData mappedSubscriptionData) {
        return stripeSubscriptionRepository.save(mappedSubscriptionData);
    }

    protected StripeCheckoutSessions getSavedStripeCheckoutSession(StripeCheckoutSessions mappedStripeCheckoutSessions) {
        return stripeCheckoutRepository.save(mappedStripeCheckoutSessions);
    }

    protected StripeSubscriptionData getSubscriptionDataBySubscriptionId(String subscriptionId, ErrorMessages errorMsg) {
        return stripeSubscriptionRepository.findBySubscriptionId(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(errorMsg, "", subscriptionId));
    }

    protected StripeSubscriptionData getSubscriptionDataBySubscriptionIdOrCreateNew(String subscriptionId) {
        return stripeSubscriptionRepository.findBySubscriptionId(subscriptionId)
                .orElse(new StripeSubscriptionData());
    }

    protected Organization getOrganizationByOrganizationId(Long organizationId, ErrorMessages errorMessage) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(errorMessage, organizationId));
    }

    protected StripeCheckoutSessions getStripeCheckoutSessions(String sessionId) {
        return stripeCheckoutRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH59, sessionId));
    }

    protected StripeInvoice saveStripeInvoice(StripeInvoice stripeInvoice){
        return stripeInvoiceRepository.save(stripeInvoice);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public void handleSubscriptionCreated(Event event) {
        Subscription createdCustomerSubscription = (Subscription) event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH60));
        log.info("Event : customer.subscription.created. Created Customer Subscription id: {}", createdCustomerSubscription.getId());
        log.info("Event : customer.subscription.created. Created Customer id: {}", createdCustomerSubscription.getCustomer());
        StripeSubscriptionData mappedSubscriptionData = getStripeSubscriptionData(createdCustomerSubscription);
        log.info("Event : customer.subscription.created. Subscription created and mapped");
        StripeSubscriptionData savedCreatedCustomerSubscription = getSavedCreatedCustomerSubscription(mappedSubscriptionData);
        log.info("Event : customer.subscription.created. Saved Created Customer Subscription : {}", savedCreatedCustomerSubscription);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class)
    public void handleSubscriptionUpdated(Event event) {
        Subscription updatedCustomerSubscription = (Subscription) event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH61));
        log.info("Event : customer.subscription.updated. Created Customer Subscription id: {}", updatedCustomerSubscription.getId());
        log.info("Event : customer.subscription.updated. Created Customer id: {}", updatedCustomerSubscription.getCustomer());
        StripeSubscriptionData mappedDatabaseStripeSubscriptionData = getStripeSubscriptionData(updatedCustomerSubscription);
        log.info("Event : customer.subscription.updated. Subscription created and mapped");
        StripeSubscriptionData savedUpdatedCustomerSubscription = getSavedCreatedCustomerSubscription(mappedDatabaseStripeSubscriptionData);
        log.info("Event : customer.subscription.updated. Saved Updated Customer Subscription : {}", savedUpdatedCustomerSubscription);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCompletedCheckoutSession(Event event) throws Exception {
        Session createdSession = (Session) event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH62));
        log.info("Event: checkout.session.completed. Session created successfully: {}", createdSession.getId());
        log.info("Event: checkout.session.completed. Subscription Success: {}", createdSession.getSubscription());
        processCheckoutSession(createdSession);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCompletedCheckoutExpired(Event event) {
        Session expiredSession = (Session) event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH63));
        log.info("Session expired: {}", expiredSession.getId());
    }

    private void processCheckoutSession(Session createdSession) throws Exception {
        // 1. Fetch Stripe data (non-transactional)
        Subscription stripeSubscription = getStripeSubscription(createdSession);

        // 2. Update database records (short transaction)
        StripeCheckoutSessions savedCheckoutSession = updateCheckoutSession(createdSession, stripeSubscription);
        log.info("Event : checkout.session.completed. SavedCheckoutSession : {}", savedCheckoutSession);
        String action = createdSession.getMetadata().get("action");
        if ("upgrade".equals(action) || "upgradeSeat".equals(action)) {
            return;
        }

        // 3. Handle post-processing (separate transaction)
        handlePostCheckoutActions(savedCheckoutSession);
    }

    private StripeCheckoutSessions updateCheckoutSession(Session retrievedSession, Subscription stripeSubscription) throws StripeException {
        StripeCheckoutSessions stripeCheckoutSessions = getStripeCheckoutSessions(retrievedSession.getId());

        // Update core fields
        updateCoreSessionFields(retrievedSession, stripeCheckoutSessions);

        // Handle coupon if exists
        handleSessionCoupons(retrievedSession, stripeCheckoutSessions);

        // Handle subscription data if exists
        if (!ObjectUtils.isEmpty(stripeSubscription)) {
            StripeSubscriptionData subscriptionData = getStripeSubscriptionData(stripeSubscription);
            StripeSubscriptionData savedStripeSubscriptionData = getSavedCreatedCustomerSubscription(subscriptionData);
            associateSubscriptionWithSession(stripeCheckoutSessions, savedStripeSubscriptionData);
        }

        String action = retrievedSession.getMetadata().get("action");
        if ("upgrade".equals(action) || "upgradeSeat".equals(action)) {
            return handleUpgradeAction(retrievedSession, stripeCheckoutSessions, action);
        }
        return saveAndLogCheckoutSession(stripeCheckoutSessions);
    }

    // Helper methods for updateCheckoutSession
    private void updateCoreSessionFields(Session session, StripeCheckoutSessions checkoutSession) {
        checkoutSession.setExpiresAt(Instant.ofEpochSecond(session.getExpiresAt())
                .atZone(ZoneId.systemDefault()).toLocalDate());
        checkoutSession.setPaymentStatus(session.getPaymentStatus());
        checkoutSession.setInvoice(session.getInvoice());
        checkoutSession.setStatus(session.getStatus());
    }

    private void handleSessionCoupons(Session session, StripeCheckoutSessions checkoutSession) throws StripeException {
        if (!ObjectUtils.isEmpty(session.getDiscounts()) && !ObjectUtils.isEmpty(session.getDiscounts().get(0))) {
            StripeCoupon stripeCoupon = stripeCouponRepository.findById(checkoutSession.getCoupons().get(0).getId())
                    .orElse(new StripeCoupon());
            String couponId = session.getDiscounts().get(0).getCoupon();
            Coupon retrieveCoupon = stripeService.retreiveStripeCoupon(couponId);

            stripeCoupon.setCouponName(retrieveCoupon.getName());
            stripeCoupon.setCouponId(retrieveCoupon.getId());
            stripeCoupon.setPercentOff(retrieveCoupon.getPercentOff());
            stripeCoupon.setCheckoutSession(checkoutSession);

            StripeCoupon savedStripeCoupon = stripeCouponRepository.save(stripeCoupon);
            log.info("Event: checkout.session.completed. SavedStripeCoupon : {}", savedStripeCoupon);
        }
    }

    private void associateSubscriptionWithSession(StripeCheckoutSessions session, StripeSubscriptionData subscriptionData) {
        session.setSubscriptionData(subscriptionData);
        subscriptionData.addCheckoutSessions(session);
        log.info("Event: checkout.session.completed. Subscription saved successfully: {}", subscriptionData);
    }

    private StripeCheckoutSessions handleUpgradeAction(Session session, StripeCheckoutSessions checkoutSession, String action) {
        try {
            StripeSubscriptionData subscriptionData = "upgrade".equals(action)
                    ? handleUpgradeCompletion(session)
                    : handleUpgradeSeatCompletion(session);

            associateSubscriptionWithSession(checkoutSession, subscriptionData);
            StripeCheckoutSessions savedSession = saveAndLogCheckoutSession(checkoutSession);

            stripeEmailService.sendSubscriptionUpgradeDowngradeSuccessEmailAndNotification(subscriptionData);
            updateUpgradeRecordStatus(savedSession);
            updateCreditsToOrganizationsWhenSubscriptionUpdated(subscriptionData);

            return savedSession;
        } catch (StripeException e) {
            log.error("Something went wrong while {} subscription in stripe: {}", action, e.getMessage());
        } catch (Exception e) {
            log.error("Something went wrong while {} subscription or sending email {}", action, e.getMessage());
        }
        return checkoutSession;
    }

    private StripeCheckoutSessions saveAndLogCheckoutSession(StripeCheckoutSessions session) {
        StripeCheckoutSessions savedSession = stripeCheckoutRepository.save(session);
        log.info("Event: checkout.session.completed. SavedStripeCheckoutSession : {}", savedSession);
        return savedSession;
    }

    private void updateUpgradeRecordStatus(StripeCheckoutSessions session) {
        upgradeRecordRepository.findByCheckoutSessionId(session.getSessionId())
                .ifPresent(subscriptionUpgradeRecord -> {
                    subscriptionUpgradeRecord.setStatus("completed");
                    StripeSubscriptionUpgradeRecord savedRecord = upgradeRecordRepository.save(subscriptionUpgradeRecord);
                    log.info("Event: checkout.session.completed. SavedStripeSubscriptionUpgradeRecord : {}", savedRecord);
                });
    }

    private StripeSubscriptionData handleUpgradeCompletion(Session session) throws StripeException {
        Subscription updatedStripeSubscription = getUpdatedStripeSubscription(session);
        StripeSubscriptionData subscriptionData = getStripeSubscriptionData(updatedStripeSubscription);
        StripeSubscriptionData savedStripeSubscriptionData = getSavedCreatedCustomerSubscription(subscriptionData);
        log.info("Event: checkout.session.completed. Upgraded Subscription and saved successfully: {}", savedStripeSubscriptionData);
        return savedStripeSubscriptionData;
    }

    private StripeSubscriptionData handleUpgradeSeatCompletion(Session session) throws StripeException {
        Subscription updatedStripeSubscription = getUpdatedStripeSubscription(session);
        StripeSubscriptionData subscriptionData = getStripeSubscriptionData(updatedStripeSubscription);
        StripeSubscriptionData savedStripeSubscriptionData = getSavedCreatedCustomerSubscription(subscriptionData);
        log.info("Event: checkout.session.completed. Upgraded Subscription and seat and saved successfully: {}", savedStripeSubscriptionData);
        return savedStripeSubscriptionData;
    }

    private Subscription getUpdatedStripeSubscription(Session session) throws StripeException {
        String subscriptionId = session.getMetadata().get("subscription_id");
        String newPriceId = session.getMetadata().get("new_price_id");
//        PaymentIntent intent = PaymentIntent.retrieve(session.getPaymentIntent());
        Subscription subscription = Subscription.retrieve(subscriptionId);
//        long paidAmount = intent.getAmount();
//        log.info("Event: checkout.session.completed. paid Amount : {}", paidAmount);
        return subscription.update(
                SubscriptionUpdateParams.builder()
                        .addItem(SubscriptionUpdateParams.Item.builder()
                                .setId(subscription.getItems().getData().get(0).getId())
                                .setPrice(newPriceId)
                                .build())
                        .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.NONE)
                        .build()
        );
    }


    private void   handlePostCheckoutActions(StripeCheckoutSessions savedCheckoutSession) throws Exception {
        // Handle payment mode
        if (savedCheckoutSession.getMode().equals(StripeMode.PAYMENT)) {
            stripeEmailService.sendPaymentSuccessEmailAndNotificationForPayment(savedCheckoutSession);
            log.info("Event: checkout.session.completed. Email and notification sent for mode Payment");
        }
        // Handle subscription mode
        else if (savedCheckoutSession.getMode().equals(StripeMode.SUBSCRIPTION) && !ObjectUtils.isEmpty(savedCheckoutSession.getSubscriptionData())) {
            handleSubscriptionActions(savedCheckoutSession);
            log.info("Event: checkout.session.completed. Email and notification sent after purchasing subscription");
        }
        updateCreditsToOrganizationsForEventCheckoutSession(savedCheckoutSession);
    }

    private void handleSubscriptionActions(StripeCheckoutSessions checkoutSession) throws Exception {
        stripeEmailService.sendPaymentSuccessEmailAndNotificationAfterSubscriptionPurchased(checkoutSession.getSubscriptionData());
    }

    // Helper method
    private Subscription getStripeSubscription(Session stripeSession) throws StripeException {
        return !ObjectUtils.isEmpty(stripeSession.getSubscription()) ?
                Subscription.retrieve(stripeSession.getSubscription()) :
                null;
    }


    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void handleInvoicePaidEvent(Event event) throws StripeException {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH64));

        // Skip zero-amount trial invoices — prevents Rs 0 / $0 entries in DB
        if (invoice.getAmountPaid() != null && invoice.getAmountPaid() == 0L) {
            log.info("Skipping zero-amount trial invoice: {}", invoice.getId());
            return;
        }

        StripeSubscriptionData subscriptionData = getSubscriptionDataBySubscriptionId(invoice.getSubscription(), ErrorMessages.SH55);

        // Update billing period dates
        subscriptionData.setStartOn(Instant.ofEpochSecond(invoice.getPeriodStart())
                .atZone(ZoneId.systemDefault())
                .toLocalDate());

        subscriptionData.setEndOn(Instant.ofEpochSecond(invoice.getPeriodEnd())
                .atZone(ZoneId.systemDefault())
                .toLocalDate());

        // If transitioning from TRIALING to ACTIVE
        StripeSubscriptionData savedSubscriptionForInvoicePaid;
        if (subscriptionData.getStatus().equals(StripeSubscriptionStatus.TRIALING)) {
            subscriptionData.setStatus(StripeSubscriptionStatus.ACTIVE);
            savedSubscriptionForInvoicePaid = getSavedCreatedCustomerSubscription(subscriptionData);
            log.info("Event : invoice.paid. Status TRIALING to ACTIVE. savedSubscriptionForInvoicePaid : {}", savedSubscriptionForInvoicePaid);
            log.info("Event : invoice.paid. Subscription was in trail phase and now it upgraded to active and Invoice paid");
        } else if ("subscription_update".equals(invoice.getBillingReason()) && subscriptionData.getStatus().equals(StripeSubscriptionStatus.PENDING_DOWNGRADE)) {
            subscriptionData.setStatus(StripeSubscriptionStatus.ACTIVE);
            savedSubscriptionForInvoicePaid = getSavedCreatedCustomerSubscription(subscriptionData);
            log.info("Event : invoice.paid. Status PENDING_DOWNGRADE to ACTIVE. savedSubscriptionForInvoicePaid : {}", savedSubscriptionForInvoicePaid);
            log.info("Event : invoice.paid. Subscription upgraded or downgraded and Invoice paid");
        } else {
            Subscription retrievedStripeSubscription = Subscription.retrieve(invoice.getSubscription());
            subscriptionData.setStatus(StripeSubscriptionStatus.valueOf(retrievedStripeSubscription.getStatus().toUpperCase()));
            savedSubscriptionForInvoicePaid = getSavedCreatedCustomerSubscription(subscriptionData);
            log.info("Event : invoice.paid. savedSubscriptionForInvoicePaid : {}", savedSubscriptionForInvoicePaid);
        }

        StripeInvoice stripeInvoice = StripeInvoice.builder()
                .invoiceId(invoice.getId())
                .subscriptionId(invoice.getSubscription())
                .customerId(invoice.getCustomer())
                .invoicePdfUrl(invoice.getInvoicePdf())
                .build();

        StripeInvoice savedStripeInvoice = saveStripeInvoice(stripeInvoice);
        log.info("Event : invoice.paid. SavedStripeInvoice: {}", savedStripeInvoice);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void handleInvoicePaymentFailedEvent(Event event) throws Exception {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH65));
        Subscription retrievedStripeSubscription = Subscription.retrieve(invoice.getSubscription());

        // Update subscription status to "past_due"
        StripeSubscriptionData foundSubscriptionDataFromDatabase = getSubscriptionDataBySubscriptionId(invoice.getSubscription(), ErrorMessages.SH83);
        if (invoice.getBillingReason() != null && invoice.getBillingReason().equalsIgnoreCase("subscription_cycle")) {
            String customerId = invoice.getCustomer();

            // 3. Check if subscription period has ended (1 year)
            long subscriptionEnd = retrievedStripeSubscription.getCurrentPeriodEnd(); // Unix timestamp
            long now = System.currentTimeMillis() / 1000;

            // If current period has ended (or very close), it's a renewal failure
            if (now >= subscriptionEnd) {
                sendPaymentLinkEmail(customerId, invoice, foundSubscriptionDataFromDatabase);
                log.info("Event : invoice.payment_failed. Email send for renewal with link to continue subscription");
            }
        }

        foundSubscriptionDataFromDatabase.setStatus(StripeSubscriptionStatus.valueOf(retrievedStripeSubscription.getStatus().toUpperCase()));
        StripeSubscriptionData savedSubscriptionForInvoicePaymentFailed = getSavedCreatedCustomerSubscription(foundSubscriptionDataFromDatabase);

        log.info("Event : invoice.payment_failed. SavedSubscriptionForInvoicePaymentFailed : {}", savedSubscriptionForInvoicePaymentFailed);
    }

    public void sendPaymentLinkEmail(String customerId, Invoice invoice, StripeSubscriptionData stripeSubscriptionData) throws Exception {

        // 1. Get customer email from your database or Stripe
        StripeCustomer customer = stripeCustomerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH66, customerId));

        if (ObjectUtils.isEmpty(customer) && ObjectUtils.isEmpty(customer.getCustomerEmail())) {
            throw new SharkdomException(ErrorMessages.SH117);
        }

        // 2. Send email
        stripeEmailService.sendEmailForSubscriptionAutoRenewalFailed(customer, invoice, stripeSubscriptionData);
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void handlePaymentIntentSucceeded(Event event) throws StripeException {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH67));
        log.info("Event: payment_intent.succeeded. PaymentIntent : {}", paymentIntent.getId());
        log.info("Metadata : {}", paymentIntent.getMetadata());

        String invoiceId = paymentIntent.getInvoice();
        if (!ObjectUtils.isEmpty(invoiceId)) {
            Invoice invoice = Invoice.retrieve(invoiceId);
            Subscription subscription = Subscription.retrieve(invoice.getSubscription());

            // 2. Update subscription status in your database
            StripeSubscriptionData mappedSubscriptionData = getStripeSubscriptionData(subscription);
            StripeSubscriptionData savedSubscriptionForPaymentIntentSucceeded = getSavedCreatedCustomerSubscription(mappedSubscriptionData);
            log.info("Event: payment_intent.succeeded. savedSubscriptionForPaymentIntentSucceeded: {}", savedSubscriptionForPaymentIntentSucceeded);
        }

        // 3. Update payment method if changed during confirmation
        if (!ObjectUtils.isEmpty(paymentIntent.getSetupFutureUsage())) {
            Customer customer = Customer.retrieve(paymentIntent.getCustomer());
            CustomerUpdateParams params = CustomerUpdateParams.builder()
                    .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
                            .setDefaultPaymentMethod(paymentIntent.getPaymentMethod())
                            .build())
                    .build();
            customer.update(params);
        }

        StripePayment payment = stripePaymentRepository.findByPaymentIntentId(paymentIntent.getId())
                .orElseGet(() -> createNewPayment(paymentIntent));

        payment.setStatus(StripePaymentStatus.SUCCEEDED);
        payment.setAmount(((double) paymentIntent.getAmount() / 100));
        payment.setCurrency(paymentIntent.getCurrency());
        StripePayment savedStripePayment = stripePaymentRepository.save(payment);
        log.info("Event: payment_intent.succeeded. SavedStripePayment: {}", savedStripePayment);
    }


    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void handleChargeSucceeded(Event event) {
        Charge charge = (Charge) event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH68));

        stripePaymentRepository.findByPaymentIntentId(charge.getPaymentIntent())
                .ifPresent(payment -> {
                    payment.setChargeId(charge.getId());
                    payment.setPaymentMethod(charge.getPaymentMethodDetails().getType());
                    payment.setReceiptUrl(charge.getReceiptUrl());
                    StripePayment savedStripePayment = stripePaymentRepository.save(payment);
                    log.info("Event: charge.succeeded. SavedStripePayment: {}", savedStripePayment);
                });
    }

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class)
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void handlePaymentIntentFailed(Event event) throws StripeException {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH69));

        log.info("Event: payment_intent.payment_failed. Metadata : {}", paymentIntent.getMetadata());

        // 1. Retrieve related invoice and subscription
        String invoiceId = paymentIntent.getInvoice();
        if (!ObjectUtils.isEmpty(invoiceId)) {
            Invoice invoice = Invoice.retrieve(invoiceId);
            Subscription subscription = Subscription.retrieve(invoice.getSubscription());

            // 2. Update subscription status
            StripeSubscriptionData mappedSubscriptionData = getStripeSubscriptionData(subscription);
            StripeSubscriptionData savedSubscriptionForPaymentFailed = getSavedCreatedCustomerSubscription(mappedSubscriptionData);
            log.info("Event: payment_intent.payment_failed. savedSubscriptionForPaymentFailed : {}", savedSubscriptionForPaymentFailed);
            handleFinalFailure(subscription.getId());
        }

        // 3. Determine failure reason
        StripeError failure = paymentIntent.getLastPaymentError();
        String failureReason = "";
        if (failure != null) {
            failureReason = failure.getMessage();
            log.info("DeclineCode: {}", failure.getDeclineCode());
            log.info("Code: {}", failure.getCode());
            log.info("AdviceCode: {}", failure.getAdviceCode());
            log.info("LastResponse: {}", failure.getLastResponse());
            if (failure.getPaymentMethod() != null) {
                failureReason += "Payment failed (Payment Method: " + failure.getPaymentMethod().getId() + ")";
                log.info("Failure Reason: {}", failureReason);
            }
        }

        StripePayment stripePayment = stripePaymentRepository.findByPaymentIntentId(paymentIntent.getId())
                .orElseGet(() -> createNewFailedPayment(paymentIntent));
        log.info("Event: payment_intent.payment_failed. StripePayment: {}", stripePayment);

        stripePayment.setStatus(StripePaymentStatus.FAILED);
        stripePayment.setAmount(((double) paymentIntent.getAmount() / 100));
        stripePayment.setCurrency(paymentIntent.getCurrency());
        if (!ObjectUtils.isEmpty(paymentIntent.getLastPaymentError())) {
            stripePayment.setFailureMessage(paymentIntent.getLastPaymentError().getMessage());
            stripePayment.setDeclineCode(paymentIntent.getLastPaymentError().getDeclineCode());
            stripePayment.setAdviceCode(paymentIntent.getLastPaymentError().getAdviceCode());
        }
        StripePayment savedStripePayment = stripePaymentRepository.save(stripePayment);
        log.info("Event: payment_intent.payment_failed. SavedStripePayment: {}", savedStripePayment);
    }

    private StripePayment createNewFailedPayment(PaymentIntent paymentIntent) {
        StripePayment payment = new StripePayment();
        payment.setPaymentIntentId(paymentIntent.getId());
        payment.setStatus(StripePaymentStatus.FAILED);
        payment.setCustomerId(paymentIntent.getCustomer());
        payment.setAmount(((double) paymentIntent.getAmount() / 100));
        payment.setCurrency(paymentIntent.getCurrency());
        if (!ObjectUtils.isEmpty(paymentIntent.getLastPaymentError())) {
            payment.setFailureMessage(paymentIntent.getLastPaymentError().getMessage());
            payment.setDeclineCode(paymentIntent.getLastPaymentError().getDeclineCode());
            payment.setAdviceCode(paymentIntent.getLastPaymentError().getAdviceCode());
        }
        return payment;
    }


    private void handleFinalFailure(String subscriptionId) {
        try {
            // 1. Downgrade to free plan or cancel
            Subscription subscription = Subscription.retrieve(subscriptionId);
            Subscription cancelSubscription = subscription.cancel();

            // 2. Update database
            StripeSubscriptionData mappedSubscriptionData = getStripeSubscriptionData(cancelSubscription);
            StripeSubscriptionData savedSubscriptionCanceledForPaymentFailed = getSavedCreatedCustomerSubscription(mappedSubscriptionData);
            log.info("Event: payment_intent.payment_failed. savedSubscriptionCanceledForPaymentFailed : {}", savedSubscriptionCanceledForPaymentFailed);

        } catch (StripeException e) {
            log.error("Failed to handle final payment failure", e);
        }
    }

    private StripePayment createNewPayment(PaymentIntent paymentIntent) {
        return StripePayment.builder()
                .paymentIntentId(paymentIntent.getId())
                .customerId(paymentIntent.getCustomer())
                .amount(((double) paymentIntent.getAmount() / 100))
                .currency(paymentIntent.getCurrency())
                .status(StripePaymentStatus.CREATED)
                .build();
    }

    public StripeSubscriptionData getStripeSubscriptionData(Subscription updatedSubscription) {
        StripeSubscriptionData foundSubscriptionData = getSubscriptionDataBySubscriptionIdOrCreateNew(updatedSubscription.getId());
        log.info("Mapped Subscription Data: {}", foundSubscriptionData);
        log.info("To Map Subscription received from stripe : {}", updatedSubscription.getId());

        StripeCustomer foundAndSaveStripeCustomer = foundSubscriptionData.getCustomer();
        if (ObjectUtils.isEmpty(foundAndSaveStripeCustomer)) {
            foundAndSaveStripeCustomer = stripeCustomerRepository.findByCustomerId(updatedSubscription.getCustomer())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH70, foundSubscriptionData.getSubscriptionId()));
            foundSubscriptionData.setCustomer(foundAndSaveStripeCustomer);
            foundSubscriptionData.setOrganizationId(foundAndSaveStripeCustomer.getOrganizationId());
        }
        foundSubscriptionData.setSubscriptionId(updatedSubscription.getId());
        foundSubscriptionData.setTrialPeriodDays(
                ObjectUtils.isEmpty(updatedSubscription.getItems().getData().get(0).getPlan().getTrialPeriodDays())
                        ? calculateTrailDaysForStatusTrailing(foundSubscriptionData, updatedSubscription)
                        : updatedSubscription.getItems().getData().get(0).getPlan().getTrialPeriodDays());
        foundSubscriptionData.setStatus(StripeSubscriptionStatus.valueOf(updatedSubscription.getStatus().toUpperCase()));
        Long unitAmount = updatedSubscription.getItems().getData().get(0).getPrice().getUnitAmount();
        foundSubscriptionData.setAmount(ObjectUtils.isEmpty(unitAmount) ? null : (unitAmount / 100));
        foundSubscriptionData.setStartOn(Instant.ofEpochSecond(updatedSubscription.getStartDate())
                .atZone(ZoneId.systemDefault()).toLocalDate());
        foundSubscriptionData.setEndOn(Instant.ofEpochSecond(updatedSubscription.getCurrentPeriodEnd())
                .atZone(ZoneId.systemDefault()).toLocalDate());
        foundSubscriptionData.setCancelledOn(!ObjectUtils.isEmpty(updatedSubscription.getCanceledAt())
                ? Instant.ofEpochSecond(updatedSubscription.getCanceledAt()).atZone(ZoneId.systemDefault()).toLocalDate()
                : null);
        foundSubscriptionData.setCancellationReason(!ObjectUtils.isEmpty(updatedSubscription.getCancellationDetails())
                ? updatedSubscription.getCancellationDetails().getReason()
                : null);
        foundSubscriptionData.setLatestInvoice(!ObjectUtils.isEmpty(updatedSubscription.getLatestInvoice()) ? updatedSubscription.getLatestInvoice() : null);
        PriceEntity price;
        if (ObjectUtils.isEmpty(foundSubscriptionData.getPrice())) {
            price = new PriceEntity();
            price.setStripePriceId(ObjectUtils.isEmpty(updatedSubscription.getItems().getData().get(0).getPrice().getId()) ? null : updatedSubscription.getItems().getData().get(0).getPrice().getId());
            StripePlanType planType = null;
            try {
                planType = stripePlanConfigurationService.getPlanTypeByPriceId(updatedSubscription.getItems().getData().get(0).getPrice().getId());
            } catch (ResourceNotFoundException e) {
                log.warn("No plan type found for dynamic priceId: {}", updatedSubscription.getItems().getData().get(0).getPrice().getId());
            }
            price.setPlanType(planType);
            PriceEntity savedPriceEntity = stripePriceRepository.save(price);
            log.info("PriceEntity : {}", savedPriceEntity);
            foundSubscriptionData.setPrice(savedPriceEntity);
        } else {
            price = stripePriceRepository.findById(foundSubscriptionData.getPrice().getId()).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH71));
            price.setStripePriceId(ObjectUtils.isEmpty(updatedSubscription.getItems().getData().get(0).getPrice().getId()) ? null : updatedSubscription.getItems().getData().get(0).getPrice().getId());
            StripePlanType planType = null;
            try {
                planType = stripePlanConfigurationService.getPlanTypeByPriceId(price.getStripePriceId());
            } catch (ResourceNotFoundException e) {
                log.warn("No plan type found for dynamic priceId: {}", price.getStripePriceId());
            }
            price.setPlanType(planType);
            PriceEntity savedPriceEntity = stripePriceRepository.save(price);
            foundSubscriptionData.setPrice(savedPriceEntity);
            log.info("PriceSet : {}", price);
        }
        StripePlanType resolvedPlanType = foundSubscriptionData.getPrice().getPlanType();
        if (resolvedPlanType != null) {
            Long seatAssignedByPlanType = stripePlanConfigurationService.getSeatByPlanType(resolvedPlanType);
            foundSubscriptionData.setSeatAssign(seatAssignedByPlanType);
            foundSubscriptionData.setSeatLeft(seatAssignedByPlanType);
        } else {
            log.warn("Skipping seat assignment — no plan type for subscription: {}", updatedSubscription.getId());
        }
        return foundSubscriptionData;
    }

    private static Long calculateTrailDaysForStatusTrailing(StripeSubscriptionData foundSubscriptionData, Subscription updatedSubscription) {
        StripeSubscriptionStatus stripeSubscriptionStatus = StripeSubscriptionStatus.valueOf(updatedSubscription.getStatus().toUpperCase());
        Long startTimestamp = updatedSubscription.getCurrentPeriodStart();
        Long endTimestamp = updatedSubscription.getCurrentPeriodEnd();
        if (stripeSubscriptionStatus.equals(StripeSubscriptionStatus.TRIALING)) {
            Instant startDate = Instant.ofEpochSecond(startTimestamp);
            Instant endDate = Instant.ofEpochSecond(endTimestamp);
            return ChronoUnit.DAYS.between(startDate, endDate);
        }
        return foundSubscriptionData.getTrialPeriodDays();
    }

    private void updateCreditsToOrganizationsForEventCheckoutSession(StripeCheckoutSessions stripeCheckoutSessions) {
        Set<Long> organizationIds = stripeCheckoutSessions.getCustomer().getOrganizationId();
        List<StripePlanType> stripePlanType = stripeCheckoutSessions.getLineItems().stream()
                .map(lineItemEntity -> lineItemEntity.getPrice().getPlanType())
                .toList();
        if (!ObjectUtils.isEmpty(organizationIds)) {
            organizationIds.forEach(organizationId -> {
                Organization organization = getOrganizationByOrganizationId(organizationId, ErrorMessages.SH80);
                if (stripeCheckoutSessions.getMode().equals(StripeMode.SUBSCRIPTION)) {
                    organization.setSubscribed(true);
                } else if (stripeCheckoutSessions.getMode().equals(StripeMode.PAYMENT)) {
                    organization.setSubscribed(false);
                }
                Credits credits = organization.getCredits();
                if (credits == null) {
                    credits = new Credits();
                }
                Credits updatedCredits = updateCreditsFromPlan(stripePlanType, credits);
                log.info("Event: checkout.session.completed. Updated credits for organization after payment or subscription purchased: {}", updatedCredits);
                organization.setCredits(updatedCredits);
                Organization updatedOrganization = organizationRepository.save(organization);
                log.info("Event: checkout.session.completed. Updated organization after payment or subscription purchased: {}", updatedOrganization);
            });
        }

    }

    private Credits updateCreditsFromPlan(List<StripePlanType> plan, Credits credits) {
        int aiProposalCredits = credits.getAiProposalAllocated();
        int playgroundCredits = credits.getPlaygroundAllocated();
        int collaborationSent = credits.getCollaborationsAllocated();
        for (StripePlanType stripePlanType : plan) {
            StripePlanConfiguration stripePlanConfiguration = stripePlanConfigurationRepository.findById(stripePlanType)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH53, stripePlanType, "So credits will not be updated"));
            aiProposalCredits += stripePlanConfiguration.getAiProposalCredits();
            playgroundCredits += stripePlanConfiguration.getPlaygroundCredits();
            collaborationSent += stripePlanConfiguration.getCollaborationSent();
        }
        credits.setAiProposalAllocated(aiProposalCredits);
        credits.setPlaygroundAllocated(playgroundCredits);
        credits.setCollaborationsAllocated(collaborationSent);
        return credits;
    }

    private void updateCreditsToOrganizationsWhenSubscriptionUpdated(StripeSubscriptionData stripeSubscriptionData) {
        Set<Long> organizationIds = stripeSubscriptionData.getOrganizationId();
        StripePlanType stripePlanType = stripeSubscriptionData.getPrice().getPlanType();
        if (!ObjectUtils.isEmpty(organizationIds)) {
            organizationIds.forEach(organizationId -> {
                Organization organization = organizationRepository.findById(organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH72, stripeSubscriptionData.getSubscriptionId(), organizationId));

                organization.setSubscribed(!stripeSubscriptionData.getStatus().equals(StripeSubscriptionStatus.CANCELED) &&
                        !stripeSubscriptionData.getStatus().equals(StripeSubscriptionStatus.INCOMPLETE) &&
                        !stripeSubscriptionData.getStatus().equals(StripeSubscriptionStatus.INCOMPLETE_EXPIRED));

                Credits credits = organization.getCredits();
                if (credits == null) {
                    credits = new Credits();
                }
                Credits updatedCredits = resetCreditsFromPlanAfterSubscriptionUpdate(stripeSubscriptionData.getStatus(), stripePlanType, credits);
                log.info("Updated Credits for Organization after subscription updated: {}", updatedCredits);
                organization.setCredits(updatedCredits);
                Organization updatedOrganization = organizationRepository.save(organization);
                log.info("Updated Organization after subscription updated: {}", updatedOrganization);
            });
        }
    }

    private Credits resetCreditsFromPlanAfterSubscriptionUpdate(StripeSubscriptionStatus status, StripePlanType stripePlanType, Credits credits) {
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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH53, stripePlanType, "So credits will not be updated"));
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

    @Recover
    public void handleRetryExhausted(ObjectOptimisticLockingFailureException ex) {
        // Fallback logic after all retries fail (e.g., log, notify, or escalate)
        log.error("Failed to update after retries. Error: {}", ex.getMessage());
    }

    public void handleCustomerBalanceTransactionCreated(Event event) {
        CustomerBalanceTransaction transaction =
                (CustomerBalanceTransaction) event.getDataObjectDeserializer().getObject()
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH73));

        if (transaction.getAmount() > 0 && transaction.getDescription().contains("downgrade")) {
            log.info("Event : customer.balance_transaction.created. Email sent to customer id {} of amount {}{}", transaction.getCustomer(), transaction.getAmount() / 100, transaction.getCurrency());

            // Notify customer about their credit
//            stripeEmailService.sendDowngradeCreditNotification(
//                    transaction.getCustomer(),
//                    transaction.getAmount(),
//                    transaction.getCurrency()
//            );
        }
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void reconcileSubscriptions() {
        List<StripeSubscriptionData> subscriptions = stripeSubscriptionRepository.findLast10SubscriptionsNative();
        for (StripeSubscriptionData sub : subscriptions) {
            try {
                Subscription stripeSub = Subscription.retrieve(sub.getSubscriptionId());
                StripeSubscriptionData stripeSubscriptionData = getStripeSubscriptionData(stripeSub);
                getSavedCreatedCustomerSubscription(stripeSubscriptionData);
            } catch (StripeException e) {
                log.error("Something went from stripe {}", e.getMessage());
            } catch (Exception e) {
                log.error("Something went wrong while running subscription update schedule {}", e.getMessage());
            }
        }
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
}
