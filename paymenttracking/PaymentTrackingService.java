package com.sharkdom.service.paymenttracking;

import com.google.gson.Gson;
import com.sharkdom.entity.paymenttracking.RazorpayTrackingEntity;
import com.sharkdom.entity.paymenttracking.RazorpayTrackingTestEntity;
import com.sharkdom.repository.paymenttracking.RazorpayTrackingRepository;
import com.sharkdom.repository.paymenttracking.RazorpayTrackingTestRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentTrackingService {
    private final RazorpayTrackingRepository razorpayTrackingRepository;
    private final RazorpayTrackingTestRepository razorpayTrackingTestRepository;

    public PaymentTrackingService(RazorpayTrackingRepository razorpayTrackingRepository, RazorpayTrackingTestRepository razorpayTrackingTestRepository) {
        this.razorpayTrackingRepository = razorpayTrackingRepository;
        this.razorpayTrackingTestRepository = razorpayTrackingTestRepository;
    }

    public void trackRazorpayPayment(String referralCode, Map<Object, Object> payment) {
        RazorpayTrackingEntity razorpayTracking = (RazorpayTrackingEntity) buildRazorpayTrackingEntity(referralCode, payment, RazorpayTrackingEntity.class);
        razorpayTrackingRepository.save(razorpayTracking);
    }

    public void trackRazorpayPaymentTest(String referralCode, Map<Object, Object> payment) {
        RazorpayTrackingTestEntity razorpayTrackingTest = (RazorpayTrackingTestEntity) buildRazorpayTrackingEntity(referralCode, payment, RazorpayTrackingTestEntity.class);
        razorpayTrackingTestRepository.save(razorpayTrackingTest);
    }

    // Common method to build entity
    private Object buildRazorpayTrackingEntity(String organizationCode, Map<Object, Object> payment, Class<?> entityClass) {
        String accountId = (String) payment.get("account_id");
        String eventType = (String) payment.get("event");

        Long organizationId = null;
        if (payment.containsKey("organizationId")) {
            organizationId = ((Number) payment.get("organizationId")).longValue();
        }

        Map<String, Object> payload = (Map<String, Object>) payment.get("payload");
        Map<String, Object> paymentEntity = (Map<String, Object>) payload.get("payment");
        Map<String, Object> paymentDetails = (Map<String, Object>) paymentEntity.get("entity");

        // Extracting details
        String paymentId = (String) paymentDetails.get("id");
        String orderId = (String) paymentDetails.get("order_id");
        int amount = (int) paymentDetails.get("amount");
        String currency = (String) paymentDetails.get("currency");
        String status = (String) paymentDetails.get("status");
        String method = (String) paymentDetails.get("method");
        String bank = (String) paymentDetails.get("bank");
        String contact = (String) paymentDetails.get("contact");
        String email = (String) paymentDetails.get("email");

        String affiliateCode = null;
        Map<String, Object> notes = (Map<String, Object>) paymentDetails.get("notes");
        if (notes != null) {
            affiliateCode = (String) notes.get("affiliateCode");
        }

        // Convert the entire payload back to JSON
        String payloadJson = new Gson().toJson(payload);

        if (entityClass == RazorpayTrackingEntity.class) {
            return RazorpayTrackingEntity.builder()
                    .organizationCode(organizationCode)
                    .organizationId(organizationId)
                    .affiliateCode(affiliateCode)
                    .accountId(accountId)
                    .eventType(eventType)
                    .paymentId(paymentId)
                    .orderId(orderId)
                    .amount(amount)
                    .currency(currency)
                    .status(status)
                    .method(method)
                    .bank(bank)
                    .contact(contact)
                    .email(email)
                    .payload(payloadJson)
                    .build();
        } else if (entityClass == RazorpayTrackingTestEntity.class) {
            return RazorpayTrackingTestEntity.builder()
                    .organizationCode(organizationCode)
                    .organizationId(organizationId)
                    .affiliateCode(affiliateCode)
                    .accountId(accountId)
                    .eventType(eventType)
                    .paymentId(paymentId)
                    .orderId(orderId)
                    .amount(amount)
                    .currency(currency)
                    .status(status)
                    .method(method)
                    .bank(bank)
                    .contact(contact)
                    .email(email)
                    .payload(payloadJson)
                    .build();
        } else {
            throw new IllegalArgumentException("Invalid entity class: " + entityClass);
        }
    }

}
