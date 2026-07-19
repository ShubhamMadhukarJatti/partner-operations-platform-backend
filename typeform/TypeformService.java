package com.sharkdom.service.typeform;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.typeform.TypeformEvent;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.typeform.*;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.typeform.TypeFormEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TypeformService {

    @Autowired
    private RestTemplate restTemplateConfig;

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private TypeFormEventRepository typeFormEventRepository;

    @Value("${typeform.api.forms-url}")
    private String typeformApiUrl;

    @Value("${typeform.api.token-url}")
    private String tokenUrl;

    @Value("${typeform.auth.client-id}")
    private String clientId;

    @Value("${typeform.auth.client-secret}")
    private String clientSecret;

    @Value("${typeform.auth.redirect-uri}")
    private String redirectUri;

    @Value("${typeform.webhook.url}")
    private String webhookUrl;

    //    private static final String TYPEFORM_API_URL = "https://api.typeform.com/forms";
//    private static final String TOKEN_URL = "https://api.typeform.com/oauth/token";
//    private static final String CLIENT_ID = "9DmrycPkk2dQrUqBoabFMcyEi9Zkh2jP2Z1bpFZwByJf";
//    private static final String CLIENT_SECRET = "2RpLk3N3JvfAdmoJZ9xXChmmboL4Q8dpPDgTHDkxmqkm";
//    private static final String REDIRECT_URI = "https://dev.sharkdom.com/typeForm/callback";
//    private static final String WEBHOOK_URL = "https://dev.sharkdom.com/typeForm/webhook";
    private static String accessToken = "";
    private static String refreshToken = "";

    public Map<String, String> getAuthorizationUrl() {
        String encodedScopes = URLEncoder.encode("offline forms:write forms:read responses:read webhooks:write webhooks:read", StandardCharsets.UTF_8);
        String authorizationUrl = "https://api.typeform.com/oauth/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri + "&scope=" + encodedScopes;
        Map<String, String> map = new HashMap<>();
        map.put("AuthorizationUrl", authorizationUrl);
        return map;
    }

    public TypeFormTokenResponse handleAuthorization(String code) throws JSONException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setBearerAuth(authorization);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplateConfig.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                String tokenType = jsonResponse.getString("token_type");
                Integer expiresIn = jsonResponse.getInt("expires_in");
                accessToken = jsonResponse.getString("access_token");
                refreshToken = jsonResponse.getString("refresh_token");
                TypeFormTokenResponse typeFormTokenResponse = new TypeFormTokenResponse(tokenType, accessToken, expiresIn, refreshToken);
                log.info("Token response: {} ", typeFormTokenResponse);
                return typeFormTokenResponse;
            } else {
                log.error("Failed to retrieve access token");
                return new TypeFormTokenResponse();
            }
        } catch (JSONException e) {
            log.error("Failed to parse JSON response: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Failed Authorization: ", e);
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

    public TypeformResponse createTypeform(Long organizationId, String customAccessToken, TypeformRequest request) throws JSONException {
        try {
            IntegrationDetails integrationDetails = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.TYPEFORM);
            if (ObjectUtils.isEmpty(customAccessToken)) {
                customAccessToken = integrationDetails.getRefreshToken();
            } else if (ObjectUtils.isEmpty(customAccessToken) && ObjectUtils.isEmpty(integrationDetails.getRefreshToken())) {
                customAccessToken = accessToken;
            }
            log.info("CustomeAccessToken: {}", accessToken);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(customAccessToken);

            JSONObject formJson = new JSONObject();
            formJson.put("title", request.getTitle());

            JSONArray fields = new JSONArray();

            fields.put(new JSONObject().put("type", "short_text").put("title", "Name of your brand"));
            fields.put(new JSONObject().put("type", "short_text").put("title", "Name of your App (can be the same as your brand name)"));
            fields.put(new JSONObject().put("type", "long_text").put("title", "What does your app/company specialize in?")
                    .put("properties", new JSONObject().put("description", "Mention key areas of expertise separated by a comma (e.g., SEO, Marketing, Web Development, etc.)")));

            JSONArray choices = new JSONArray()
                    .put(new JSONObject().put("label", "Co-partnered post on our Socials (LinkedIn, Meta)"))
                    .put(new JSONObject().put("label", "Partnered Blog Post on the website"))
                    .put(new JSONObject().put("label", "Newsletter (We are planning to start one soon!)"))
                    .put(new JSONObject().put("label", "Webinar"))
                    .put(new JSONObject().put("label", "Other"));

            fields.put(new JSONObject().put("type", "multiple_choice").put("title", "Let us know the marketing activities that you wish to be part of:")
                    .put("properties", new JSONObject().put("allow_multiple_selection", true).put("choices", choices)));

            fields.put(new JSONObject().put("type", "short_text").put("title", "UTM Tracking for Website")
                    .put("properties", new JSONObject().put("description", "Helps you gauge visitors from Nector")));
            fields.put(new JSONObject().put("type", "short_text").put("title", "UTM Tracking for Blog")
                    .put("properties", new JSONObject().put("description", "Helps you gauge visitors from Nector")));
            fields.put(new JSONObject().put("type", "long_text").put("title", "Notable brands that you have worked with")
                    .put("properties", new JSONObject().put("description", "Mention the names separated by a comma (,) and this information helps us with social proof")));

            formJson.put("fields", fields);

            HttpEntity<String> entity = new HttpEntity<>(formJson.toString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(typeformApiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                String displayLink = jsonResponse.getJSONObject("_links").getString("display");
                String formId = jsonResponse.getString("id");
                String tag = formId;
                addWebhook(formId, webhookUrl, tag, customAccessToken);
                log.info("response {} ", response);
                return new TypeformResponse(displayLink);
            } else {
                log.error("Failed to Create Form");
                return new TypeformResponse();
            }
        } catch (JSONException e) {
            log.error("Failed to parse JSON response: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Something went wrong, failed to Create Form: ", e);
            throw new ServiceException(ErrorMessages.SH116, e.getMessage());
        }
    }

    public TypeFormWebhookResponse addWebhook(String formId, String webhookUrl, String tag, String customAccessToken) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(customAccessToken);

        JSONObject webhookData = new JSONObject();
        webhookData.put("url", webhookUrl);
        webhookData.put("enabled", true);
        webhookData.put("event_types", new JSONObject().put("form_response_partial", true).put("form_response", true));
//        webhookData.put("secret", "your_webhook_secret");

        String webhookEndpoint = typeformApiUrl + "/" + formId + "/webhooks/" + tag;
        HttpEntity<String> entity = new HttpEntity<>(webhookData.toString(), headers);
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(webhookEndpoint, HttpMethod.PUT, entity, String.class);
            JSONObject jsonResponse = new JSONObject(response.getBody());
            TypeFormWebhookResponse webhookResponse = new TypeFormWebhookResponse(
                    jsonResponse.getString("created_at"),
                    jsonResponse.getBoolean("enabled"),
                    new TypeFormWebhookResponse.EventTypes(
                            jsonResponse.getJSONObject("event_types").getBoolean("form_response"),
                            jsonResponse.getJSONObject("event_types").getBoolean("form_response_partial")
                    ),
                    jsonResponse.getString("form_id"),
                    jsonResponse.getString("id"),
                    jsonResponse.getString("tag"),
                    jsonResponse.getString("updated_at"),
                    jsonResponse.getString("url"),
                    jsonResponse.getBoolean("verify_ssl")
            );
            log.info("Webhook response: {}", response.getBody());
            log.info("Webhook webhookResponse : {}", webhookResponse);
            if (response.getStatusCode() == HttpStatus.OK) {
                return webhookResponse;
            } else {
                log.error("Failed to add webhook. Status: " + response.getStatusCode());
                return new TypeFormWebhookResponse();
            }
        } catch (Exception e) {
            log.error("Error adding webhook: ", e);
            throw e;
        }
    }

    public TypeformResponse retrieveWebhook(String formId) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        JSONObject webhookData = new JSONObject();
        webhookData.put("url", webhookUrl);
        webhookData.put("enabled", true);

        String webhookEndpoint = typeformApiUrl + "/" + formId + "/webhooks";
        HttpEntity<String> entity = new HttpEntity<>(webhookData.toString(), headers);
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(webhookEndpoint, HttpMethod.GET, entity, String.class);
            log.info("Webhook response: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error adding webhook: ", e);
            return new TypeformResponse("Error adding webhook: " + e.getMessage());
        }

        if (response.getStatusCode() == HttpStatus.OK) {
            return new TypeformResponse("Webhook successfully added");
        } else {
            return new TypeformResponse("Failed to add webhook. Status: " + response.getStatusCode());
        }
    }

    public TypeFormRefreshResponse refreshAccessToken() throws Exception {
        try {
            if (refreshToken.isEmpty()) {
                throw new Exception("No refresh token available.");
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "refresh_token");
            params.add("refresh_token", refreshToken);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("scope", "forms:write forms:read responses:read webhooks:write webhooks:read");

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
            ResponseEntity<String> response;


            response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);
            log.info("Refresh token response: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                accessToken = jsonResponse.getString("access_token");
                refreshToken = jsonResponse.getString("refresh_token");
                log.info("New Access Token: " + accessToken);
                log.info("Refresh Token: " + refreshToken);
                return new TypeFormRefreshResponse(accessToken, refreshToken);
            } else {
                log.error("Failed to refresh access token. Status Code: " + response.getStatusCode());
                return new TypeFormRefreshResponse();
            }
        } catch (JSONException e) {
            log.error("Failed to parse JSON response: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Error refreshing access token: ", e);
            throw e;
        }
    }

    public String receiveWebhook(String payload) throws JSONException {
        JSONObject jsonResponse = new JSONObject(payload);
        String eventId = jsonResponse.getString("event_id");
        String eventType = jsonResponse.getString("event_type");
        JSONObject formResponse = jsonResponse.getJSONObject("form_response");
        log.info("EventType {}", eventType);
        log.info("Form Reponse: {}", formResponse.toString());
//        Map<String, Object> mapformResponse = (Map<String, Object>) jsonResponse.getJSONObject("form_response");
        if (ObjectUtils.isEmpty(eventType) || ObjectUtils.isEmpty(formResponse)) {
            return "Invalid payload";
        }

//        String eventType = (String) payload.get("event_type");
//        Map<String, Object> formResponse = (Map<String, Object>) payload.get("form_response");

        switch (eventType) {
            case "form_response":
                TypeformEvent typeformEvent = new TypeformEvent();
                typeformEvent.setEventId(eventId);
                typeformEvent.setEventDetail(eventType);
                typeformEvent.setFormResponse(formResponse.toString());
                typeFormEventRepository.save(typeformEvent);
                processFormResponse(formResponse);
                break;
            case "form_properties_updated":
                log.info("Form updated: {}", (String) jsonResponse.getJSONObject("form_response").getString("form_id"));
                break;
            default:
                log.warn("Unhandled event type: {}", eventType);
        }
        return eventType;
    }

    private void processFormResponse(JSONObject formResponse) throws JSONException {
        String formId = formResponse.getString("form_id");
        log.info("Received response from form: {}", formId);
    }

}
