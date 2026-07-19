package com.sharkdom.service.payment;


import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.sharkdom.config.AppProperties;
import com.sharkdom.config.WebSocketHandler;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.PlanType;
import com.sharkdom.constants.subscription.SubscriptionStatus;
import com.sharkdom.entity.configuration.Configuration;
import com.sharkdom.entity.credits.Credits;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.payment.SubscriptionEntity;
import com.sharkdom.entity.subscription.Subscription;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.payment.StripeCheckoutRequest;
import com.sharkdom.model.subscription.CreateSubscription;
import com.sharkdom.repository.configuration.ConfigurationRepository;
import com.sharkdom.repository.credits.CreditsRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.payment.RazorpaySubscriptionRepository;
import com.sharkdom.repository.subscription.SubscriptionRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.notification.NotificationService;
import com.sharkdom.util.date.DateUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.sharkdom.constants.Constants.PAYMENT_SUCCESS_TEMPLATE;

@Service
@Slf4j
public class PaymentService {
    private final SubscriptionRepository subscriptionRepository;
    private final OrganizationRepository organizationRepository;
    private final DateUtil dateUtil;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final CreditsRepository creditsRepository;
    private RazorpayClient razorpayClient;
    @Value("${razorPay.keyId}")
    String razorPayKeyId;
    @Value("${razorPay.keySecret}")
    String razorPayKeySecret;
    @Value("${stripe.secret}")
    private String stripeSecret;
    @Value("${web.url}")
    private String stripeUrl;
    @Value("${app.environment.proxy_url}")
    private String baseUrl;
    private final RazorpaySubscriptionRepository razorpaySubscriptionRepository;
    private final WebSocketHandler webSocketHandler;
    private final ConfigurationRepository configurationRepository;
    private final NotificationService notificationService;


    public PaymentService(SubscriptionRepository subscriptionRepository, OrganizationRepository organizationRepository, DateUtil dateUtil, EmailService emailService, AppProperties appProperties, CreditsRepository creditsRepository, RazorpaySubscriptionRepository razorpaySubscriptionRepository, WebSocketHandler webSocketHandler, ConfigurationRepository configurationRepository, NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.organizationRepository = organizationRepository;
        this.dateUtil = dateUtil;
        this.emailService = emailService;
        this.appProperties = appProperties;
        this.creditsRepository = creditsRepository;
        this.razorpaySubscriptionRepository = razorpaySubscriptionRepository;
        this.webSocketHandler = webSocketHandler;
        this.configurationRepository = configurationRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Subscription savePaymentDetails(HttpServletRequest httpServletRequest, Map<Object, Object> payment) {
        try {
            razorpayClient = new RazorpayClient(razorPayKeyId, razorPayKeySecret);
            String paymentId = payment.get("razorpay_payment_id").toString();
            String orderId = payment.get("razorpay_order_id").toString();
            String signature = payment.get("razorpay_signature").toString();

            JSONObject params = new JSONObject();
            params.put("razorpay_order_id", orderId);
            params.put("razorpay_payment_id", paymentId);
            params.put("razorpay_signature", signature);

            if (!Utils.verifyPaymentSignature(params, razorPayKeySecret)) {
                throw new SharkdomException(ErrorMessages.SH114);
            }

            var razorpaySubscription = razorpaySubscriptionRepository.findBySubscriptionId(orderId);
            var paymentDetails = razorpayClient.payments.fetch(paymentId);
            long paymentAmount = Long.parseLong(paymentDetails.get("amount").toString()) / 100;

            return processPaymentDetails(paymentId, paymentAmount, razorpaySubscription.getOrganizationId(),
                    razorpaySubscription.getPlanType().name(), "razorpay");
        } catch (Exception e) {
            throw new SharkdomException(ErrorMessages.SH116, e.getMessage());
        }
    }

    @Transactional
    public Subscription handleStripePaymentEvent(Map<String, Object> stripeEvent) {
        try {
            Map<String, Object> eventData = (Map<String, Object>) stripeEvent.get("data");
            Map<String, Object> paymentIntent = (Map<String, Object>) eventData.get("object");

            String paymentId = paymentIntent.get("id").toString();
            long paymentAmount = Long.parseLong(paymentIntent.get("amount").toString()) / 100;
            Map<String, String> metadata = (Map<String, String>) paymentIntent.get("metadata");

            if (!metadata.containsKey("planType") || !metadata.containsKey("organizationId")) {
                throw new SharkdomException(ErrorMessages.SH115);
            }

            String planType = metadata.get("planType");
            Long organizationId = Long.parseLong(metadata.get("organizationId"));

            return processPaymentDetails(paymentId, paymentAmount, organizationId, planType, "stripe");
        } catch (Exception e) {
            throw new SharkdomException(ErrorMessages.SH116, e.getMessage());
        }
    }

    private Subscription processPaymentDetails(String paymentId, long paymentAmount, Long organizationId, String planType, String paymentGateway) {
        Optional<Organization> organizationOptional = organizationRepository.findById(organizationId);
        if (organizationOptional.isEmpty()) {
            throw new SharkdomException(ErrorMessages.SH79, organizationId);
        }

        Organization organization = organizationOptional.get();

        //if subscription id already exist
        if (subscriptionRepository.existsSubscriptionByTransactionId(paymentId)) {
            return subscriptionRepository.findByTransactionId(paymentId);
        }

        // Get or create credits
        Credits credits = creditsRepository.findByOrganizationId(organizationId);
        if (credits == null) {
            credits = new Credits();
        }
        PlanType plan = PlanType.valueOf(planType);
        credits = getCreditsFromPlan(plan, credits);

        organization.setCredits(credits);//important
        organization.setSubscribed(true);//important
        organizationRepository.save(organization);

        // Determine subscription dates
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = planType.endsWith("_YEARLY") ? currentDate.plusYears(1) :
                planType.endsWith("_TRIAL") ? currentDate.plusDays(14) :
                        currentDate.plusMonths(1);

        Subscription subscription = Subscription.builder()
                .organizationId(organizationId)
                .planCode(planType)
                .amount(paymentAmount)
                .startOn(currentDate)
                .endOn(endDate)
                .transactionId(paymentId)
                .status(SubscriptionStatus.ACTIVE)
                .additionalInfo(paymentGateway).build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        sendPaymentSuccessEmailAndNotification(organization, savedSubscription);

        return savedSubscription;
    }


    private Credits getCreditsFromPlan(PlanType plan, Credits credits) {
        credits.setAiProposalAllocated(plan.getAiProposalCredits());
        credits.setPlaygroundAllocated(plan.getPlaygroundCredits());
        credits.setCollaborationsAllocated(plan.getCollaborationSent());
        return credits;
    }

    private void sendPaymentSuccessEmailAndNotification(Organization organization, Subscription subscription) {
        String templateCode = appProperties.getEmailTemplateCodeForEvent(PAYMENT_SUCCESS_TEMPLATE);

        emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                .templateCode(templateCode)
                .organizationName(organization.getName())
                .subscriptionName(subscription.getPlanCode())
                .subscriptionRenewal(subscription.getEndOn())
                .subscriptionBenefits("http://doc.sharkdom.com/pricing")
                .subscriptionPrice(subscription.getAmount())
                .organizationIds(List.of(organization.getId())).build(), null, 1L, 1L);

        Notification notification = Notification.builder()
                .subject("Subscription bought")
                .body("Congrats! You have successfully bought a subscription.")
                .forWeb(true)
                .organizationId(organization.getId())
                .build();

        webSocketHandler.sendMessageToUser(organization.getId(), notification);
        notificationService.create(notification);
    }

    @Transactional
    public Map<String, String> createSubscription(CreateSubscription createSubscription) {
        try {
            int amount = getPlanPrice(createSubscription.getPlanType());

            PlanType planType = createSubscription.getPlanType();
            JSONObject paymentRequest = new JSONObject();
            paymentRequest.put("currency", "INR");
            LocalDateTime now = LocalDateTime.now();
            if (createSubscription.getReferralCode() != null || createSubscription.getPlanType().name().equals(PlanType.FREE.name())) {
                if (createSubscription.getReferralCode() != null) {
                    var configuration = configurationRepository.findByKey(createSubscription.getReferralCode());
                    if (configuration.isPresent()) {
                        var configResponse = configuration.get();
                        if (Arrays.stream(PlanType.values()).anyMatch(planTypeValue -> planTypeValue.name().equalsIgnoreCase(configResponse.getType()))) {
                            if (Integer.parseInt(configResponse.getValue()) > 0) {
                                planType = PlanType.valueOf(configResponse.getType());
                                configResponse.setValue(String.valueOf(Integer.parseInt(configResponse.getValue()) - 1));
                                amount = PlanType.FREE.getAmount();
                                configurationRepository.save(configResponse);
                            }
                        }
                    }
                } else {
                    amount = PlanType.FREE.getAmount();
                }
            }
            JSONObject notes = new JSONObject();
            notes.put("organizationId", createSubscription.getOrganizationId());
            notes.put("planType", planType);
            paymentRequest.put("notes", notes);
            razorpayClient = new RazorpayClient(razorPayKeyId, razorPayKeySecret);
            paymentRequest.put("amount", amount);
            var paymentLink = razorpayClient.orders.create(paymentRequest);
            var id = paymentLink.get("id").toString();
            var subscriptionEntity = SubscriptionEntity.builder().subscriptionId(id).organizationId(createSubscription.getOrganizationId()).planType(planType).build();
            razorpaySubscriptionRepository.save(subscriptionEntity);
            return Map.of("subscriptionId", paymentLink.get("id").toString());
        } catch (Exception e) {
            log.error("exception occurred {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public int getPlanPrice(PlanType planType) {
        // Check if the planType is an upgrade plan (contains "_")
        if (planType.name().contains("_") && !planType.name().contains("TRIAL")) {
            // Split the upgrade plan to get current and new plans
            String[] plans = planType.name().split("_");

            if (plans.length == 2) {
                String currentPlanType = plans[0];
                String newPlanType = plans[1];

                // Fetch values from the repository for both current and new plans
                var currentPlanRes = configurationRepository.findAllByKeyAndType(currentPlanType, "NEW_PRICING").stream().findFirst();
                var newPlanRes = configurationRepository.findAllByKeyAndType(newPlanType, "NEW_PRICING").stream().findFirst();

                // Calculate the difference if both plans are found
                if (currentPlanRes.isPresent() && newPlanRes.isPresent()) {
                    int currentPlanPrice = Integer.parseInt(currentPlanRes.get().getValue()) * 100;
                    int newPlanPrice = Integer.parseInt(newPlanRes.get().getValue()) * 100;

                    // Return the difference in price for upgrade
                    return Math.max(newPlanPrice - currentPlanPrice, 0);
                }
            }
        } else {
            var res = configurationRepository.findAllByKeyAndType(planType.name(), "NEW_PRICING").stream().findFirst();
            return res.map(val -> Integer.parseInt(val.getValue()) * 100).orElse(500); // Default to 500 if not found
        }

        // Fallback in case something goes wrong
        return 500;
    }

    public ResponseEntity<Map<String, Object>> createStripeCheckoutSession(StripeCheckoutRequest request) throws StripeException {
        Stripe.apiKey = stripeSecret;

        try {
            Map<String, Object> metadata = Map.of(
                    "organizationId", request.getOrganizationId(),
                    "planType", request.getPlanType().name()
            );

            List<Object> lineItems = List.of(Map.of(
                    "price_data", Map.of(
                            "currency", request.getCurrency(),
                            "unit_amount", Math.round(request.getAmount() * 100),
                            "product_data", Map.of(
                                    "name", String.format("Sharkdom %s Plan", StringUtils.remove(request.getPlanType().name(), "_TRIAL"))
                            )
                    ),
                    "quantity", 1
            ));

            Map<String, Object> params = new HashMap<>();
            params.put("line_items", lineItems);
            params.put("mode", "payment");
            params.put("metadata", metadata);
            params.put("success_url", stripeUrl);
            params.put("cancel_url", stripeUrl);
            params.put("payment_intent_data", Map.of(
                    "metadata", metadata // Ensure it propagates to the PaymentIntent
            ));
            System.out.println("Line Items: " + lineItems);
            System.out.println("Params: " + params);

            Session session = Session.create(params);
            Map<String, Object> response = Map.of(
                    "sessionId", session.getId(),
                    "url", session.getUrl()
            );

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            throw e;
        }catch (Exception e){
            throw new SharkdomException(ErrorMessages.SH116, e.getMessage());
        }
    }

    public Configuration updatePricing(PlanType planType, String amount) {
        var optionalConfiguration = configurationRepository.findAllByKeyAndType(planType.name(), "NEW_PRICING").stream().findFirst();
        if (optionalConfiguration.isPresent()) {
            var configuration = optionalConfiguration.get();
            configuration.setValue(amount);
            return configurationRepository.save(configuration);
        }
        return null;
    }
}

