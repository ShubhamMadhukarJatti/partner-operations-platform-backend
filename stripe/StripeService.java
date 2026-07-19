package com.sharkdom.service.stripe;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.stripe.StripeMode;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.stripe.LineItemEntityDto;
import com.sharkdom.model.stripe.StripeCouponDto;
import com.sharkdom.model.stripe.StripeCustomerDto;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class StripeService {

    private final StripePlanConfigurationService stripePlanConfigurationService;

    PaymentMethod.Card getCustomerPaymentMethod(String customerId) throws StripeException {
        // First get the customer
        Customer customer = getStripeCustomer(customerId);

        // Get the default payment method ID
        String paymentMethodId = customer.getInvoiceSettings().getDefaultPaymentMethod();

        if (paymentMethodId == null) {
            // If no default, check payment methods attached to customer
            PaymentMethodCollection paymentMethods = PaymentMethod.list(
                    PaymentMethodListParams.builder()
                            .setCustomer(customerId)
                            .setType(PaymentMethodListParams.Type.CARD)
                            .build()
            );

            if (!paymentMethods.getData().isEmpty()) {
                paymentMethodId = paymentMethods.getData().get(0).getId();
            } else {
                throw new SharkdomException(ErrorMessages.SH113, customerId);
            }
        }
        // Retrieve the payment method
        PaymentMethod paymentMethod = getStripePaymentMethod(paymentMethodId);

        return paymentMethod.getCard();
    }

    public PaymentMethod getStripePaymentMethod(String paymentMethodId) throws StripeException {
        return PaymentMethod.retrieve(paymentMethodId);
    }

    public Customer getStripeCustomer(String customerId) throws StripeException {
        return Customer.retrieve(customerId);
    }

    PaymentMethod.Card getSubscriptionPaymentMethod(String subscriptionId) throws StripeException {
        Subscription subscription = getStripeSubscription(subscriptionId);

        // Get latest invoice
        Invoice invoice = Invoice.retrieve(subscription.getLatestInvoice());

        // Get payment intent from invoice
        String paymentIntentId = invoice.getPaymentIntent();
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        // Get payment method from payment intent
        String paymentMethodId = paymentIntent.getPaymentMethod();
        PaymentMethod paymentMethod = getStripePaymentMethod(paymentMethodId);

        return paymentMethod.getCard();
    }

    Customer updateCustomerDefaultPaymentMethod(String customerId, String paymentMethodId) throws StripeException {
        // Attach the payment method to the customer
        PaymentMethod paymentMethod = getStripePaymentMethod(paymentMethodId);
        paymentMethod.attach(PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build());

        // Update customer's default payment method
        Customer customer = getStripeCustomer(customerId);
        CustomerUpdateParams params = CustomerUpdateParams.builder()
                .setInvoiceSettings(CustomerUpdateParams.InvoiceSettings.builder()
                        .setDefaultPaymentMethod(paymentMethodId)
                        .build())
                .build();
        return customer.update(params);
    }

    Subscription updateSubscriptionPaymentMethod(String subscriptionId, String paymentMethodId) throws StripeException {
        Subscription subscription = getStripeSubscription(subscriptionId);

        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setDefaultPaymentMethod(paymentMethodId)
                .build();

        return subscription.update(params);
    }

    Subscription getStripeSubscription(String subscriptionId) throws StripeException {
        return Subscription.retrieve(subscriptionId);
    }

    Customer createStripeCustomer(StripeCustomerDto stripeCustomerDto) throws StripeException {
        String customerName = stripeCustomerDto.getCustomerName();
        String customerEmail = stripeCustomerDto.getCustomerEmail();
        String customerPhoneNumber = stripeCustomerDto.getCustomerPhoneNumber();
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(customerEmail)
                .setName(customerName)
                .setPhone(customerPhoneNumber)
                .build();
        return Customer.create(params);
    }

//    Customer updateStripeCustomerForGst(String customerId) throws StripeException {
//        Customer customer = Customer.retrieve(customerId);
//        CustomerUpdateParams customerParams = CustomerUpdateParams.builder()
//                .setTax(
//                        Collections.singletonList(
//                                CustomerUpdateParams.TaxExempt.builder()
//                                        .setType(CustomerUpdateParams.TaxIdData.Type.IN_GST)
//                                        .setValue("22AAAAA0000A1Z5")
//                                        .build()
//                        )
//                )
//                .build();
//        return customer.update(customerParams);
//    }

    Coupon createStripeCoupon(String couponCode, BigDecimal percentOff) throws StripeException {

        CouponCreateParams params = CouponCreateParams.builder()
                .setName(couponCode)
//                .setId(couponCode) // This makes the coupon code human-readable
                .setPercentOff(percentOff)
                .setDuration(CouponCreateParams.Duration.FOREVER)
                .build();

        return Coupon.create(params);
    }

    Coupon retreiveStripeCoupon(String couponId) throws StripeException {
        return Coupon.retrieve(couponId);
    }

    List<Coupon> retrieveAllStripeCoupon() throws StripeException{
        CouponListParams params = CouponListParams.builder()
                .setLimit(100L)
                .build();
        CouponCollection coupons = Coupon.list(params);
        return coupons.getData();
    }

    Coupon deleteStripeCoupon(String couponId) throws StripeException{
        Coupon retreiveStripeCoupon = retreiveStripeCoupon(couponId);
        return retreiveStripeCoupon.delete();
    }

    List<SessionCreateParams.LineItem> createStripeLineItemsForSession(List<LineItemEntityDto> lineItemEntityDtos) {
        return lineItemEntityDtos.stream()
                .map(this::createStripeLineItemForSession)
                .toList();
    }

    SessionCreateParams.LineItem createStripeLineItemForSession(LineItemEntityDto lineItemEntityDto) {
        return SessionCreateParams.LineItem.builder()
                .setPrice(stripePlanConfigurationService.getPriceIdByPlanType(lineItemEntityDto.getPrice().getPlanType()))
                .setQuantity(lineItemEntityDto.getQuantity())
                .build();
    }

    List<SessionCreateParams.Discount> createStripeDiscountsForSession(List<StripeCouponDto> stripeCouponDtoList) {
        return stripeCouponDtoList.stream()
                .map(this::createStripeDiscountForSession)
                .toList();
    }

    SessionCreateParams.Discount createStripeDiscountForSession(StripeCouponDto stripeCouponDto) {
        return SessionCreateParams.Discount.builder()
                .setCoupon(stripeCouponDto.getCouponId())
                .build();
    }

    List<SessionCreateParams.PaymentMethodType> createAllPaymentMethodType(List<String> paymentMethodTypes) {
        return paymentMethodTypes.stream()
                .map(value -> Arrays.stream(SessionCreateParams.PaymentMethodType.values())
                        .filter(e -> e.getValue().equalsIgnoreCase(value)) // Compare string values
                        .findFirst()
                        .orElse(null)) // If not found, return null
                .filter(Objects::nonNull) // Remove null values (invalid ones)
                .toList();
    }

    SessionCreateParams.Mode getStripeCheckoutMode(StripeMode modeDto) {
        return switch (modeDto) {
            case PAYMENT -> SessionCreateParams.Mode.PAYMENT;
            case SETUP -> SessionCreateParams.Mode.SETUP;
            case SUBSCRIPTION -> SessionCreateParams.Mode.SUBSCRIPTION;
        };
    }

    public Invoice getStripeInvoice(String invoiceId) throws StripeException {
        return Invoice.retrieve(invoiceId);
    }

    public InputStream downloadPdfAsInputStream(String pdfUrl) {
        RestTemplate restTemplate = new RestTemplate();

        // Get PDF as byte array
        ResponseEntity<byte[]> response = restTemplate.getForEntity(pdfUrl, byte[].class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // Convert byte array to InputStream
            return new ByteArrayInputStream(response.getBody());
        } else {
            throw new SharkdomException(ErrorMessages.SH153, pdfUrl);
        }
    }
}
