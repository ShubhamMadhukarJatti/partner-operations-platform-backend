package com.sharkdom.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.entity.integration.BooleanEvaluation;
import com.sharkdom.entity.integration.EvaluationEntity;
import com.sharkdom.entity.integration.PercentageEvaluation;
import com.sharkdom.model.integration.CompatibilityRequest;
import com.sharkdom.repository.integration.EvaluationEntityRepository;
import com.sharkdom.service.email.EmailService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class PublicProfileService {
    private final EvaluationEntityRepository evaluationEntityRepository;
    private final RestTemplate restTemplate;
    private final EmailService emailService;

    public PublicProfileService(EvaluationEntityRepository evaluationEntityRepository, EmailService emailService) {
        this.evaluationEntityRepository = evaluationEntityRepository;
        this.emailService = emailService;
        restTemplate = new RestTemplate();
    }


    @Async
    public void saveCompatibility(CompatibilityRequest compatibilityRequest) {
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
        assert responseEntity != null;
        var resp = mapResponse(responseEntity);
        resp.setEmail(compatibilityRequest.email());
        resp.setOrganizationName(compatibilityRequest.organizationName());
        emailService.sendPublicProfile("public_profile_evaluator", resp);
        evaluationEntityRepository.save(resp);

    }

    public EvaluationEntity mapResponse(Map<String, Object> response) {
        ObjectMapper mapper = new ObjectMapper();
        EvaluationEntity evaluationEntity = new EvaluationEntity();

        List<BooleanEvaluation> booleanEvaluations = new ArrayList<>();
        List<PercentageEvaluation> percentageEvaluations = new ArrayList<>();

        JsonNode evaluationNode = mapper.valueToTree(response.get("evaluation"));

        if (evaluationNode.isArray()) {
            for (JsonNode node : evaluationNode) {
                if (node.has("percentage")) {
                    PercentageEvaluation pe = new PercentageEvaluation();
                    pe.setPercentage(node.get("percentage").asInt());
                    pe.setType(node.get("type").asText());
                    percentageEvaluations.add(pe);
                } else {
                    node.fieldNames().forEachRemaining(fieldName -> {
                        BooleanEvaluation be = new BooleanEvaluation();
                        be.setKey(fieldName);
                        be.setValue(node.get(fieldName).asBoolean());
                        booleanEvaluations.add(be);
                    });
                }
            }
        }

        evaluationEntity.setBooleanEvaluations(booleanEvaluations);
        evaluationEntity.setPercentageEvaluations(percentageEvaluations);
        return evaluationEntity;
    }


    public List<EvaluationEntity> getCompatibilityScore(String email) {
        return evaluationEntityRepository.findAllByEmail(email);
    }
}
