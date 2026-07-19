package com.sharkdom.service.stripe;

import com.sharkdom.entity.stripe.StripePayOutBankAccount;
import com.sharkdom.model.stripe.*;
import com.sharkdom.repository.stripe.StripePayOutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class StripePayoutService {

    @Autowired
    StripePayOutRepository repository;
    private final RestTemplate restTemplate;
    private final String STRIPE_API_URL = "https://api.stripe.com/v1/accounts/acct_1RYsio2cAAGUgcPz/external_accounts";

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    public StripePayoutService() {
        this.restTemplate = new RestTemplate();
    }

    public StripePayoutAccountResponse addExternalBankAccount(PayoutBankAccountRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(stripeSecretKey);

        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("external_account[object]", "bank_account");
        formParams.add("external_account[country]", request.getCountry());
        formParams.add("external_account[currency]", request.getCurrency());
        formParams.add("external_account[account_holder_name]", request.getAccountHolderName());
        formParams.add("external_account[account_holder_type]", request.getAccountHolderType());
        formParams.add("external_account[account_number]", request.getAccountNumber());
        formParams.add("external_account[routing_number]", request.getRoutingNumber());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);

        ResponseEntity<StripePayoutAccountResponse> response = restTemplate.exchange(
                STRIPE_API_URL,
                HttpMethod.POST,
                entity,
                StripePayoutAccountResponse.class
        );
        StripePayoutAccountResponse stripeResponse = response.getBody();

        // Check for existing entry
        Optional<StripePayOutBankAccount> existing = repository.findByAccountAndRoutingNumber(
                request.getAccountNumber(), request.getRoutingNumber());

        StripePayOutBankAccount entityToSave = convertToEntity(stripeResponse);
        entityToSave.setAccount(request.getAccountNumber());
        entityToSave.setRoutingNumber(request.getRoutingNumber());

        if (existing.isPresent()) {

            entityToSave.setId(existing.get().getId());
        }

        repository.save(entityToSave);

        return stripeResponse;
    }
    private StripePayOutBankAccount convertToEntity(StripePayoutAccountResponse response) {
        StripePayOutBankAccount entity = new StripePayOutBankAccount();
        entity.setBankAccount_id(response.getId());
        entity.setAccount(response.getAccount());
        entity.setAccountHolderName(response.getAccount_holder_name());
        entity.setAccountHolderType(response.getAccount_holder_type());
        entity.setBankName(response.getBank_name());
        entity.setCountry(response.getCountry());
        entity.setCurrency(response.getCurrency());
        entity.setLast4(response.getLast4());
        entity.setFingerprint(response.getFingerprint());
        entity.setRoutingNumber(response.getRouting_number());
        entity.setStatus(response.getStatus());
        entity.setAvailablePayoutMethods(response.getAvailable_payout_methods());
        return entity;
    }

    public String createAccountOnboardingLink(String accountId, String returnUrl, String refreshUrl) {
        String endpoint = "https://api.stripe.com/v1/account_links";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(stripeSecretKey);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("account", accountId);
        params.add("refresh_url", refreshUrl);
        params.add("return_url", returnUrl);
        params.add("type", "account_onboarding");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<StripeAccountLinkResponse> response = restTemplate.postForEntity(
                endpoint,
                request,
                StripeAccountLinkResponse.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().getUrl();
        } else {
            throw new RuntimeException("Failed to create Stripe account link");
        }
    }

    public PaymentIntentResponse createPaymentIntent(int amount, String currency) {
        String url = "https://api.stripe.com/v1/payment_intents";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stripeSecretKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("amount", String.valueOf(amount));
        params.add("currency", currency);
        params.add("payment_method_types[]", "card");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<PaymentIntentResponse> response = restTemplate.postForEntity(
                url, request, PaymentIntentResponse.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to create Stripe PaymentIntent");
        }
    }
    public StripeAccountResponse createAccount(String email, String country) {
        String url = "https://api.stripe.com/v1/accounts";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stripeSecretKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("type", "express");
        params.add("country", country);
        params.add("email", email);
        params.add("capabilities[transfers][requested]", "true");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<StripeAccountResponse> response = restTemplate.postForEntity(
                url,
                request,
                StripeAccountResponse.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to create Stripe Express account");
        }
    }
}
