package com.sharkdom.aimodel.controller;

import com.sharkdom.model.ai.PersonaModelResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai-models")
public class AiModelsController {
    private final RestTemplate restTemplate;

    public AiModelsController() {
        this.restTemplate = new RestTemplate();
    }

    @PostMapping("/automation")
    @Operation(method = "POST", summary = "Scrapping Model", description = """
            - Instance Type: Basic (B1)

            """)
    public Object automation(@RequestBody AutomationRequest automationRequest) {
        Map<String, String> postRequest = new HashMap<>();
        postRequest.put("site", automationRequest.site());
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(postRequest);
        return restTemplate.exchange("https://sharkdom-automation.azurewebsites.net/automation/", HttpMethod.POST, requestEntity, Object.class).getBody();

    }

    @PostMapping("/persona")
    @Operation(method = "POST", summary = "Persona Model", description = """
            - Instance Type: Basic (B1)

            """)
    public Object persona(@RequestBody PersonaRequestModel personaRequest) {
        Map<String, List<String>> postRequest = new HashMap<>();
        postRequest.put("sites", personaRequest.sites);
        HttpEntity<Map<String, List<String>>> requestEntity = new HttpEntity<>(postRequest);
        ParameterizedTypeReference<List<PersonaModelResponse>> responseType = new ParameterizedTypeReference<>() {
        };
        List<PersonaModelResponse> responseEntity = restTemplate.exchange("https://sharkdom-persona.azurewebsites.net/persona", HttpMethod.POST, requestEntity, responseType).getBody();
        return responseEntity;
    }

    @PostMapping("/compatibility")
    @Operation(method = "POST", summary = "Compatibility Model", description = """
            - Instance Type: Basic (B1)

            """)
    public Object persona(@RequestBody CompatibilityRequestModel compatibilityRequest) {
        Map<String, Object> postRequest = new HashMap<>();
        postRequest.put("startup_info", compatibilityRequest.startupInfo());
        postRequest.put("industries", compatibilityRequest.sectors());
        postRequest.put("partnership_goal", compatibilityRequest.partnershipGoal());
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(postRequest);

        Map<String, Object> responseEntity = restTemplate.exchange(
                "https://sharkdom-api-integration.azurewebsites.net/evaluate",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }
        ).getBody();
        return responseEntity;
    }

    record AutomationRequest(String site) {
    }

    record PersonaRequestModel(List<String> sites) {
    }

    public record CompatibilityRequestModel(String startupInfo, List<String> sectors,
                                            String partnershipGoal) {
    }



}

