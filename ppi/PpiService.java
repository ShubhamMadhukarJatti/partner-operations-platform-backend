package com.sharkdom.service.ppi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.ppi.FormOperation;
import com.sharkdom.constants.ppi.FormType;
import com.sharkdom.constants.ppi.Question_Status;
import com.sharkdom.constants.ppi.ResponseType_Ppi;
import com.sharkdom.dto.FormQuestionResponse;
import com.sharkdom.dto.PartnerPortalBrandingRequest;
import com.sharkdom.dto.PartnerPortalBrandingResponse;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organization.OrganizationUserMapping;
import com.sharkdom.entity.ppi.*;

import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.model.organization.OrganizationUserMappingResponse;
import com.sharkdom.model.ppi.*;

import com.sharkdom.repository.catalogue.PartnerTierRepository;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;

import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.repository.ppi.*;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.organization.OrganizationService;

import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.sharkdom.constants.ppi.FormStatus.NOT_ASSIGNED;
import static com.sharkdom.constants.ppi.FormType.GOOGLE_FORM;
import static com.sharkdom.constants.ppi.FormType.INTERNAL_FORM;

@Slf4j
@Service
 public class PpiService {


    @Autowired
    private  CounterSaveRepository counterSaveRepository;
    @Autowired
    OptionsRepository optionsRepository;
    @Autowired
    FormDetailsRepository formDetailsRepository;
    @Autowired
    PpiRepository ppiRepository;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    InternalQuestionPpiRepository questionRepo;
    @Autowired
    InternalResponsePpiRepository responsePpiRepository;
    @Autowired
    WebhookQuestionPpiRepository webhookQuestionPpiRepository;
    @Autowired
    WebhookResponsePpiRepository webhookResponsePpiRepository;
    @Autowired
    IntegrationRepository integrationRepository;
    @Autowired
    OrganizationService organizationService;
    @Autowired
    CreateProjectPpiRepository createProjectPpiRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    LogDetailsRepository logDetailsRepository;
    @Autowired
    OrganizationUserMappingRepository organizationUserMappingRepository;
    @Autowired
    ScriptDetailsRepository scriptDetailsRepository;
    @Autowired
    FormApplyStatusRepository formApplyStatusRepository;
    @Value("${webhook.base-url}")
    String webhookBaseUrl;
    
    @Autowired
    private PartnerProgramDNSDataRepository partnerProgramDNSDataRepository;

    @Autowired
    private PartnerTierRepository partnerTierRepository;

    @Autowired
    private PartnerProgramStepperRepository partnerProgramStepperRepository;


    @Autowired
    private PartnerPortalBrandingRepository partnerPortalBrandingRepository;
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RestTemplate restTemplate = new RestTemplate();
    public Object createProject(CreateProjectRequest request) throws Exception {
        Long orgId =Util.getOrgIdFromToken();
        String url = "https://script.googleapis.com/v1/projects";


        boolean titleExists = createProjectPpiRepository.existsByTitleAndOrgId(request.getTitle(), orgId);
        if (titleExists) {
            Map<String, Object> duplicateResponse = new HashMap<>();
            duplicateResponse.put("message", "This project title already exists.");
            return duplicateResponse;
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(request.getAccessToken());

        Map<String, Object> body = new HashMap<>();
        body.put("title", request.getTitle());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {

            ResponseEntity<CreateProject> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    CreateProject.class
            );

            CreateProject createProjectResponse = response.getBody();
            if (createProjectResponse == null) {
                throw new RuntimeException("Failed to create project. Empty response.");
            }

            String webhookUrl = generateWebhookUrl(String.valueOf(orgId));
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));

            PpiEntity ppiEntity = new PpiEntity();
            ppiEntity.setScriptId(createProjectResponse.getScriptId());
            ppiEntity.setOrganization(organization);
            ppiEntity.setWebBookUrl(webhookUrl);

            String script = getString(orgId, webhookUrl);

            createProjectResponse.setWebBookUrl(webhookUrl);
            createProjectResponse.setScript(script);
            createProjectResponse.setOrgId(orgId);
            createProjectResponse.setTitle(request.getTitle()); // set title for saving

            ppiRepository.save(ppiEntity);
            createProjectPpiRepository.save(createProjectResponse);

            return createProjectResponse;

        } catch (HttpClientErrorException.Forbidden ex) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Update script access by clicking the link below");
            errorResponse.put("updateAccessUrl", "https://script.google.com/home/usersettings?pli=1");
            return errorResponse;
        } catch (Exception e) {
            throw new RuntimeException("Project creation failed: " + e.getMessage(), e);
        }
    }

    @NotNull
    private static String getString(Long orgId, String webhookUrl) {
        return "function onFormSubmit(e) {\n" +
                "  var webhookUrl = '" + webhookUrl + "';\n" +
                "  try {\n" +
                "    var resp = e && e.response;\n" +
                "    var payload = {};\n" +
                "\n" +
                "    if (resp && resp.getTimestamp) {\n" +
                "      payload.Timestamp = resp.getTimestamp().toISOString();\n" +
                "    }\n" +
                "\n" +
                "    if (resp && resp.getItemResponses) {\n" +
                "      resp.getItemResponses().forEach(function(ir) {\n" +
                "        var question = ir.getItem().getTitle();\n" +
                "        var answer   = ir.getResponse();\n" +
                "        payload[question] = answer;\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "    var options = {\n" +
                "      method: 'post',\n" +
                "      contentType: 'application/json',\n" +
                "      payload: JSON.stringify(payload),\n" +
                "      muteHttpExceptions: true\n" +
                "    };\n" +
                "\n" +
                "    var res = UrlFetchApp.fetch(webhookUrl, options);\n" +
                "    Logger.log('Posted to webhook with status %s, body: %s', res.getResponseCode(), res.getContentText());\n" +
                "  } catch (err) {\n" +
                "    Logger.log('Error: %s', (err && err.stack) || err);\n" +
                "    throw err;\n" +
                "  }\n" +
                "}\n";
    }

    public ScriptUpdateResponse updateScript(ScriptUpdateWrapper requestBody) {
        Long orgId = Util.getOrgIdFromToken();
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        if (requestBody.getScriptId() == null || requestBody.getAccessToken() == null) {
            throw new IllegalArgumentException("Script ID and Access Token are required.");
        }

        String url = "https://script.googleapis.com/v1/projects/" + requestBody.getScriptId() + "/content";

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(requestBody.getAccessToken().trim());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ScriptUpdateRequest newRequest = requestBody.getScriptUpdateRequest();

        // Prepare payload
        ScriptUpdateRequest payload = new ScriptUpdateRequest();
        payload.setFiles(newRequest.getFiles());

        HttpEntity<ScriptUpdateRequest> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<ScriptUpdateResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    entity,
                    ScriptUpdateResponse.class
            );

            // Save update status
            PpiEntity ppiEntity = ppiRepository.findOneByOrganization_Id(orgId)
                    .orElseGet(() -> {
                        PpiEntity newEntity = new PpiEntity();
                        newEntity.setOrganization(organization);
                        newEntity.setScriptId(requestBody.getScriptId());
                        return newEntity;
                    });

            ppiEntity.setAppScriptUpdate(true);
            ppiRepository.save(ppiEntity);

            return response.getBody();

        } catch (HttpClientErrorException.Forbidden ex) {
            // Specific handling for 403
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Update script access by clicking the link below: https://script.google.com/home/usersettings?pli=1",
                    ex
            );

        } catch (HttpClientErrorException ex) {
            // Generic 4xx error handling
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "Client error while updating script: " + ex.getStatusText(),
                    ex
            );

        } catch (Exception ex) {
            // Generic error
            throw new RuntimeException("Error calling Google Script API", ex);
        }
    }



    public DeploymentResponse createDeployment(DeploymentWrapper deploymentWrapper) {
        String url = "https://script.googleapis.com/v1/projects/" + deploymentWrapper.getScriptId() + "/deployments";
        Long orgId = Util.getOrgIdFromToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(deploymentWrapper.getAccessToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        DeploymentRequest deploymentRequest = deploymentWrapper.getDeploymentRequest();
        HttpEntity<DeploymentRequest> entity = new HttpEntity<>(deploymentRequest, headers);

        ResponseEntity<DeploymentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                DeploymentResponse.class
        );

        // Update PpiEntity based on organization
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        PpiEntity ppiEntity = ppiRepository.findOneByOrganization_Id( orgId)
                .orElseGet(() -> {
                    PpiEntity newEntity = new PpiEntity();
                    newEntity.setOrganization(organization);
                    newEntity.setScriptId(deploymentWrapper.getScriptId());
                    return newEntity;
                });

        ppiEntity.setDeployed(true);
        // ppiEntity.setTriggerUpdated(true);
        // ppiEntity.setWebhook(true);
        ppiRepository.save(ppiEntity);

        return response.getBody();
    }

    public CreateVersionResponse createScriptVersion(CreateVersionRequest request) {
        String url = String.format("https://script.googleapis.com/v1/projects/"+request.getScriptId()+"/versions");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(request.getAccessToken());

        // Only send the correct JSON payload (title only)
        Map<String, Object> body = new HashMap<>();
        body.put("description", request.getDescription());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);


        ResponseEntity<CreateVersionResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                CreateVersionResponse.class
        );

        return response.getBody();
    }
    public GoogleDetailsResponse getProjectMetadata(GoogleDetailsRequest request) {
        String url = "https://script.googleapis.com/v1/projects/" + request.getScriptId();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(request.getAccessToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<GoogleDetailsResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                GoogleDetailsResponse.class
        );

        return response.getBody();
    }

    public PpiEntity fetchByOrgId(Long organizationId) {
        return ppiRepository.findTopByOrganization_IdOrderByCreationTimestampDesc(organizationId)
                .orElseThrow(() -> new RuntimeException("No PPI found for organization ID: " + organizationId));
    }

    public String generateWebhookUrl(String orgId) {
        return webhookBaseUrl +"receive-webhook/"+ orgId;
    }
    @Transactional
    public QuestionResponse saveQuestion(InternalQuestion_Ppi question) {
        try {
            Long orgId = Util.getOrgIdFromToken();
            Organization org = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            question.setOrganization(org);

            Long formId;
            FormDetails formDetails;

            if (question.getFormId() != null) {

                formId = question.getFormId();
                List<FormDetails> existingForms = formDetailsRepository.findByFormId(formId);
                if (!existingForms.isEmpty()) {
                    formDetails = existingForms.get(0);
                } else {
                    throw new RuntimeException("FormDetails not found for provided formId: " + formId);
                }
            } else {

                formId = getUniqueRandom();
                String formName;
                boolean isUnique;
                do {
                    formName = generateRandomAlphaNumeric();
                    String finalFormName = formName;
                    isUnique = formDetailsRepository.findAllByOrganizationId(orgId).stream()
                            .noneMatch(fd -> finalFormName.equals(fd.getFormName()));
                } while (!isUnique);

                formDetails = saveFormDetails(formId, orgId, question.getStatus(), formName, true, false, null);
            }

            question.setFormDetails(formDetails);

            InternalQuestion_Ppi existingQuestion = questionRepo
                    .findByFormDetails_FormIdAndQuestionText(formId, question.getQuestionText());

            InternalQuestion_Ppi savedQuestion;

            if (existingQuestion != null) {
                // Update existing question
                existingQuestion.setStatus(question.getStatus());
                existingQuestion.setResponseTypePpi(question.getResponseTypePpi());

                if (existingQuestion.getOptions() != null) {
                    existingQuestion.getOptions().clear();
                } else {
                    existingQuestion.setOptions(new ArrayList<>());
                }

                if (question.getOptions() != null) {
                    for (Options opt : question.getOptions()) {
                        opt.setInternalQuestionPpi(existingQuestion);
                        opt.setFormId(formId);
                        existingQuestion.getOptions().add(opt);
                    }
                }

                existingQuestion.setStatus(Question_Status.ACTIVE);
                savedQuestion = questionRepo.save(existingQuestion);

            } else {
                long questionOrder = 1 + Optional.ofNullable(questionRepo.findByOrganization_Id(orgId)).orElse(List.of()).size();
                question.setQuestionOrder(questionOrder);

                if (question.getOptions() != null) {
                    question.getOptions().forEach(opt -> {
                        opt.setInternalQuestionPpi(question);
                        opt.setFormId(formId);
                    });
                }
                question.setStatus(Question_Status.ACTIVE);
                savedQuestion = questionRepo.save(question);
            }

            if (org.getForm() == null || !org.getForm().equals(String.valueOf(formId))) {
                org.setForm(String.valueOf(formId));
                org.setInternalFormActive(false);
                org.setFormType(INTERNAL_FORM);
                org.setResponses(null);
                org.setIsFormSubmitted(false);
                organizationRepository.save(org);
            }

            QuestionResponse response = new QuestionResponse();
            response.setResponseMessage("Question Saved Successfully");
            response.setFormId(formId);
            response.setSavedQuestion(savedQuestion);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to save question", e);
        }
    }









    public Long getUniqueRandom() {
        final Set<Integer> generated = new HashSet<>();
        final int MIN = 1;
        final int MAX = 999_999;
        if (generated.size() >= (MAX - MIN + 1)) {
            throw new RuntimeException("All unique numbers exhausted");
        }

        Long num;  
        do {
            num = ThreadLocalRandom.current().nextLong(MIN, MAX + 1);
        } while (!generated.add(Math.toIntExact(num))); // add() returns false if already present

        return num;
    }
    @Transactional
    public QuestionResponse  saveResponse(BulkSaveResponseRequest request) {
        Long orgId = request.getOrgId();

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        List<InternalResponse_Ppi> responsesToSave = new ArrayList<>();

        for (SingleQuestionResponse r : request.getResponses()) {
            ResponseType_Ppi type = ResponseType_Ppi.valueOf(r.getResponseTypePpi());

            InternalResponse_Ppi response = new InternalResponse_Ppi();
            response.setFormId(request.getFormId());
            response.setQuestionId(r.getQuestionId());
            response.setUserId(request.getUserId());
            response.setOrganization(org);
            response.setFormStatus(NOT_ASSIGNED);
            response.setResponseTypePpi(type);

            if (type == ResponseType_Ppi.SINGLETEXT || type == ResponseType_Ppi.MULTITEXT) {
                response.setResponseText(
                        Optional.ofNullable(r.getResponseText()).orElse(new ArrayList<>())
                );
            }

            // Set options if type is MCQSINGLEOPTION or MCQMULTIOPTION
            if ((type == ResponseType_Ppi.MCQSINGLEOPTION || type == ResponseType_Ppi.MCQMULTIOPTION)
                    && r.getOptions() != null && !r.getOptions().isEmpty()) {

                List<Options> options = r.getOptions().stream().map(opt -> {
                    Options option = new Options();
                    option.setOptionId(opt.getOptionId());
                    option.setValue(opt.getValue());
                    option.setFormId(request.getFormId());
                    option.setInternalResponsePpi(response);
                    return option;
                }).collect(Collectors.toList());

                response.setOptions(options);
            } else {
                response.setOptions(new ArrayList<>()); // ensure non-null list
            }

            responsesToSave.add(response);
        }

        // Save all new responses (no check for existing records)
        responsePpiRepository.saveAll(responsesToSave);

        // Mark organization as submitted (not saving responses in it to avoid circular reference)
        org.setIsFormSubmitted(true);
        org.setResponses(null);
        organizationRepository.save(org);

        QuestionResponse qr = new QuestionResponse();
        qr.setFormId(request.getFormId());
        qr.setResponseMessage("Responses saved successfully");

        return qr;
    }


    public void webhookResponseSave(String payload, Long orgId) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(payload);
        String submitterId = generateSubmissionId();
//        String formName = generateRandomAlphaNumeric();
        boolean isInternalForm = false;
        boolean isGoogleForm = true;

        log.info("In service payload: {} | Org ID: {}", payload, orgId);

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        List<ScriptDetails> scriptDetailsList = scriptDetailsRepository.findByOrgId(orgId);
        if (scriptDetailsList == null || scriptDetailsList.isEmpty()) {
            throw new RuntimeException("No ScriptDetails found for orgId: " + orgId);
        }

        ScriptDetails scriptDetails = scriptDetailsList.get(0);  // Only using the first one
        String formLink = scriptDetails.getFormId();
        String scriptId = scriptDetails.getScriptId();
        String formName = scriptDetails.getFormName();

        FormDetails formDetails = saveFormDetails(
                null, orgId, null, formName, isInternalForm, isGoogleForm, formLink
        );


        String timestampStr = root.has("Timestamp") ? root.get("Timestamp").asText() : null;
        LocalDateTime timestamp = null;
        Date date = new Date();

        if (timestampStr != null) {
            try {

                date = Date.from(OffsetDateTime.parse(timestampStr).toInstant());
                log.info("Parsed and converted timestamp: {}", date);
            } catch (Exception e) {
                log.error("Failed to parse timestamp: {}", timestampStr, e);
                throw new RuntimeException("Invalid timestamp format in payload");
            }
        }

        boolean responseSaved = false;

        Long questionOrder = 0L;
        for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            String key = entry.getKey();

            if (!"Timestamp".equalsIgnoreCase(key)) {
                String value = entry.getValue().asText();

                WebHookQuestion_Ppi question = new WebHookQuestion_Ppi();
                question.setQuestionText(key.trim());
                question.setCreationTimestamp(date);
                question.setLastUpdatedTimestamp(date);
                question.setOrganization(organization);
                question.setFormDetails(formDetails);
                question.setQuestionOrder(questionOrder + 1);
                WebHookQuestion_Ppi savedQuestion = webhookQuestionPpiRepository.save(question);

                WebHookResponse_Ppi response = new WebHookResponse_Ppi();
                response.setResponseText(Collections.singletonList(value));
                response.setCreationTimestamp(date);
                response.setLastUpdatedTimestamp(date);
                response.setQuestionId(savedQuestion.getId());
                response.setFormId(formLink);
                response.setOrganization(organization);
                response.setSubmissionId(submitterId);
                webhookResponsePpiRepository.save(response);
                responseSaved = true;
            }
            if (organization != null) {
                organization.setResponderUrl(scriptDetails.getResponderUrl());
                organization.setForm(formLink);
                organization.setIsFormSubmitted(true);
                organization.setFormType(GOOGLE_FORM);
                organizationRepository.save(organization);

            }
        }


        if (responseSaved) {
            Optional<PpiEntity> optionalPpiEntity = ppiRepository.findOneByOrganization_Id(orgId);

            if (optionalPpiEntity.isPresent()) {

                PpiEntity ppiEntity = optionalPpiEntity.get();
                ppiEntity.setWebhook(true);
                ppiEntity.setTriggerUpdated(true);
                ppiEntity.setPublish(true);
                ppiEntity.setIsCompleted(true);
                ppiRepository.save(ppiEntity);
            } else {
                log.error("No ppi found for org id " + orgId + "while putting entry in webhook");
            }
            CounterRequest counterRequest = new CounterRequest();
            counterRequest.setFormId(formLink);
            counterRequest.setIsSubmit(true);
            counterRequest.setFormType("google");
            counterRequest.setIsClick(false);
            counterRequest.setUserId("anonymous");
            incrementCounter(counterRequest);
        }
    }
    public static String generateSubmissionId() {
        return "SUB-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    public List<InternalQuestion_Ppi> fetchQuestionByOrgId() {
        Long orgId = Util.getOrgIdFromToken();

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found for ID: " + orgId));

        boolean isInternalFormActive = organization.isInternalFormActive();

        List<InternalQuestion_Ppi> listQuestion = questionRepo.findByOrganization_IdAndStatus(orgId, Question_Status.ACTIVE);

        for (InternalQuestion_Ppi question : listQuestion) {
            question.setIsInternalFormActive(isInternalFormActive);
        }

        return listQuestion;
    }


    public List<InternalResponse_Ppi> fetchResponseByOrgId() {
        Long orgId = Util.getOrgIdFromToken();
        return responsePpiRepository.findByOrganization_Id(orgId);
    }
    private FormDetails saveFormDetails(Long formId, Long orgId, Question_Status status, String formName, Boolean isInternalForm, Boolean isGoogleForm, String form) {
        Optional<FormDetails> existingFormDetails = Optional.empty();

        if (isGoogleForm) {
            existingFormDetails = formDetailsRepository.findByFormAndOrganizationId(form, orgId);
        }

        if (existingFormDetails.isPresent()) {
            return existingFormDetails.get(); // Reuse existing
        }

        FormDetails formDetails = new FormDetails();
        formDetails.setFormId(formId);
        formDetails.setOrganizationId(orgId);
        formDetails.setFormName(formName);
        formDetails.setStatus(Question_Status.DRAFT);

        if (isInternalForm) {
            formDetails.setRecentPartner("SharkDom");
            formDetails.setIsInternalForm(true);
        } else if (isGoogleForm) {
            formDetails.setForm(form);
            formDetails.setRecentPartner("");
            formDetails.setIsGoogleForm(true);
        }

        return formDetailsRepository.save(formDetails);
    }



    public Map<String, Boolean> fetchStepping() {
        Long orgId = Util.getOrgIdFromToken();
        Map<String, Boolean> mapResponse = new HashMap<>();

        boolean isGoogleFormConnected = false;
        boolean isGoogleSheetConnected = false;
        boolean isAppScriptUpdate = false;
        boolean isDeployed = false;
        boolean isTriggerUpdated = false;
        boolean isWebhook = false;
        Boolean isgoogleSheetUpdated=false;
        Boolean IsScriptCodeGenerated=false;
        Boolean isFormSelected=false;
         Boolean isCompleted=false;

        List<PpiEntity> ppiEntities = ppiRepository.findByOrganization_Id(orgId);

        for (PpiEntity ppi : ppiEntities) {
            isGoogleFormConnected |= ppi.isGoogleFormConnected();
            isGoogleSheetConnected |= ppi.isGoogleSheetConnected();
            isAppScriptUpdate |= ppi.isAppScriptUpdate();
            isDeployed |= ppi.isDeployed();
            isTriggerUpdated |= ppi.isTriggerUpdated();
            isWebhook |= ppi.isWebhook();
            isgoogleSheetUpdated |=ppi.getIsgoogleSheetUpdated();
            IsScriptCodeGenerated |=ppi.getIsScriptCodeGenerated();
            isFormSelected |=ppi.getIsFormSelected();
            isCompleted|=ppi.getIsCompleted();
        }

        mapResponse.put("isGoogleFormConnected", isGoogleFormConnected);
        mapResponse.put("isGoogleSheetConnected", isGoogleSheetConnected);
        mapResponse.put("isAppScriptUpdate", isAppScriptUpdate);
        mapResponse.put("isDeployed", isDeployed);
        mapResponse.put("isTriggerUpdated", isTriggerUpdated);
        mapResponse.put("isWebhook", isWebhook);
        mapResponse.put("isgoogleSheetUpdated", isgoogleSheetUpdated);
        mapResponse.put("IsScriptCodeGenerated", IsScriptCodeGenerated);
        mapResponse.put("isFormSelected", isFormSelected);
        mapResponse.put("isCompleted", isCompleted);
        return mapResponse;
    }

    @Transactional
    public void incrementCounter(CounterRequest counterRequest) {
        Boolean isClick = counterRequest.getIsClick();
        Boolean isSubmit = counterRequest.getIsSubmit();
        Long appliedOrgId = counterRequest.getOrgId(); // this is the org who is submitting the form
        String formType = counterRequest.getFormType();

        Long formOwnerOrgId;
        Long formId;

        if ("internal".equalsIgnoreCase(formType)) {
            try {
                formId = Long.parseLong(counterRequest.getFormId());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid internal formId: " + counterRequest.getFormId());
            }

            List<FormDetails> formDetailsList = formDetailsRepository.findByFormId(formId);
            if (formDetailsList.isEmpty()) {
                throw new RuntimeException("Internal Form not found: " + formId);
            }

            formOwnerOrgId = formDetailsList.get(0).getOrganizationId();

            if (Boolean.TRUE.equals(isSubmit)) {
                // Check if appliedOrgId has already submitted this form
                Optional<FormApplyStatus> existingApply = formApplyStatusRepository
                        .findByFormIdAndAppliedOrgId(formId, appliedOrgId);

                if (existingApply.isPresent() && Boolean.TRUE.equals(existingApply.get().getIsApplied())) {
                    throw new RuntimeException("Your organization has already submitted this form.");
                }

                // Save applied status
                FormApplyStatus status = existingApply.orElseGet(FormApplyStatus::new);
                status.setFormId(formId);
                status.setAppliedOrgId(appliedOrgId);
                status.setIsApplied(true);
                status.setAppliedOn(new Timestamp(System.currentTimeMillis()));
                formApplyStatusRepository.save(status);
            }
        } else {
            throw new RuntimeException("Unsupported form type: " + formType);
        }

        // Save counter
        CounterSave counter = new CounterSave();
        counter.setFormId(counterRequest.getFormId());
        counter.setOrgId(formOwnerOrgId);
        counter.setFormId(counterRequest.getFormId());
        counter.setUserId(counterRequest.getUserId());
        counter.setCounterOnClick(Boolean.TRUE.equals(isClick) ? 1 : 0);
        counter.setCounterOnSubmit(Boolean.TRUE.equals(isSubmit) ? 1 : 0);
        counter.setIsClick(Boolean.TRUE.equals(isClick));
        counter.setIsSubmit(Boolean.TRUE.equals(isSubmit));
        counterSaveRepository.save(counter);
    }


    public void incrementCounterExternalUser(ExternalUserCounterRequest counterRequest) {
        Boolean isClick = counterRequest.getIsClick();
        Boolean isSubmit = counterRequest.getIsSubmit();
        String formType = counterRequest.getFormType();

        Long formOwnerOrgId;
        Long formId;

         if ("internal".equalsIgnoreCase(formType)) {
            try {
                formId = Long.parseLong(counterRequest.getFormId());
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid internal formId: " + counterRequest.getFormId());
            }

            List<FormDetails> formDetailsList = formDetailsRepository.findByFormId(formId);
            if (formDetailsList.isEmpty()) {
                throw new RuntimeException("Internal Form not found: " + formId);
            }

            formOwnerOrgId = formDetailsList.get(0).getOrganizationId();

        } else {
            throw new RuntimeException("Unsupported form type: " + formType);
        }

         // Save counter
        CounterSave counter = new CounterSave();
        counter.setFormId(counterRequest.getFormId());
        counter.setOrgId(formOwnerOrgId);
        counter.setFormId(counterRequest.getFormId());
        counter.setCounterOnClick(Boolean.TRUE.equals(isClick) ? 1 : 0);
        counter.setCounterOnSubmit(Boolean.TRUE.equals(isSubmit) ? 1 : 0);
        counter.setIsClick(Boolean.TRUE.equals(isClick));
        counter.setIsSubmit(Boolean.TRUE.equals(isSubmit));
        counterSaveRepository.save(counter);
    }

    public CounterStatsResponse getOrgCounterStatsForOrgId(Long orgId) {
        List<CounterSave> orgCounters = counterSaveRepository.findAllByOrgId(orgId);

        int totalClicks = orgCounters.stream()
                .mapToInt(c -> Optional.ofNullable(c.getCounterOnClick()).orElse(0))
                .sum();

        int totalSubmits = orgCounters.stream()
                .mapToInt(c -> Optional.ofNullable(c.getCounterOnSubmit()).orElse(0))
                .sum();

        return new CounterStatsResponse(totalClicks, totalSubmits);
    }


    public CounterStatsResponse getOrgCounterStats() {
        Long orgId = Util.getOrgIdFromToken();
        List<CounterSave> orgCounters = counterSaveRepository.findAllByOrgId(orgId);

        int totalClicks = orgCounters.stream()
                .mapToInt(c -> Optional.ofNullable(c.getCounterOnClick()).orElse(0))
                .sum();

        int totalSubmits = orgCounters.stream()
                .mapToInt(c -> Optional.ofNullable(c.getCounterOnSubmit()).orElse(0))
                .sum();

        return new CounterStatsResponse(totalClicks, totalSubmits);
    }
    public CounterStatsResponse getOrgCounterStatsByFormId() {
        Long orgId = Util.getOrgIdFromToken();
        List<CounterSave> orgCounters = counterSaveRepository.findAllByOrgId(orgId);

        int totalClicks = orgCounters.stream()
                .mapToInt(c -> Optional.ofNullable(c.getCounterOnClick()).orElse(0))
                .sum();

        int totalSubmits = orgCounters.stream()
                .mapToInt(c -> Optional.ofNullable(c.getCounterOnSubmit()).orElse(0))
                .sum();

        return new CounterStatsResponse(totalClicks, totalSubmits);
    }






    public static String generateRandomAlphaNumeric() {
        int length = 8;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALPHA_NUMERIC_STRING.length());
            sb.append(ALPHA_NUMERIC_STRING.charAt(index));
        }
        return sb.toString();
    }


    public List<FormDetails> getRecentEntries() {
        Long orgId = Util.getOrgIdFromToken();

        return formDetailsRepository.findAllByOrganizationId(orgId);

    }



    public Map<String, Object> fetchAndSaveResponderUrl(ScriptRequest scriptRequest) {
        Long orgId =Util.getOrgIdFromToken();
        String url = "https://forms.googleapis.com/v1/forms/" + scriptRequest.getFormId();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(scriptRequest.getBearerToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Map<String, Object> responseBody;
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("responderUri")) {
                throw new RuntimeException("Invalid response from Google Forms API: 'responderUri' missing.");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to fetch Google Form metadata: " + ex.getMessage(), ex);
        }

        String responderUri = (String) responseBody.get("responderUri");

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found for ID: " + orgId));
        organization.setForm(responderUri);
        organization.setFormType(GOOGLE_FORM);
        organizationRepository.save(organization);

        ScriptDetails scriptDetails = scriptDetailsRepository.findByFormIdAndSheetId(
                scriptRequest.getFormId(), scriptRequest.getSheetId());

        if (scriptDetails == null) {
            scriptDetails = new ScriptDetails();
        }

        scriptDetails.setResponderUrl(responderUri);
        scriptDetails.setSheetId(scriptRequest.getSheetId());
        scriptDetails.setFormId(scriptRequest.getFormId());
        scriptDetails.setFormName(scriptRequest.getFormName());
        scriptDetails.setSheetName(scriptRequest.getSheetName());
        scriptDetails.setOrgId(orgId);
        scriptDetailsRepository.save(scriptDetails);

        try {
            Optional<PpiEntity> optionalEntity = ppiRepository.findOneByOrganization_Id(orgId);
            PpiEntity ppiEntity = optionalEntity.orElseGet(PpiEntity::new);

            ppiEntity.setFormId(scriptRequest.getFormId());
            ppiEntity.setSheetId(scriptRequest.getSheetId());
            ppiEntity.setIsFormSelected(true);
            if (ppiEntity.getOrganization() == null) {
                ppiEntity.setOrganization(organization);
            }

            ppiRepository.save(ppiEntity);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to save PPI Entity: " + ex.getMessage(), ex);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("message", "Responder details and script entry processed successfully.");
        result.put("googleFormMetadata", responseBody);

        return result;
    }


    public FormLinkCheckResponse checkFormLink(String formId, String accessToken) {
        String deploymentScript = "AKfycbzNuGvP8n93ChY7faNMJaXfR0NJkP_Rf-vFHTOoGIvpblMI022a5uMOcplWILo1tBry9Q";
        String scriptUrl = "https://script.googleapis.com/v1/scripts/" + deploymentScript + ":run";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> body = new HashMap<>();
        body.put("function", "isFormLinkedToSheet");
        body.put("parameters", List.of(formId));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    scriptUrl, HttpMethod.POST, entity, Map.class
            );

            Map responseBody = response.getBody();
            if (responseBody == null || responseBody.get("response") == null) {
                throw new RuntimeException("No 'response' found in script API result");
            }

            Map responseMap = (Map) responseBody.get("response");
            if (responseMap.get("result") == null) {
                throw new RuntimeException("No 'result' found in script API result");
            }

            Map resultMap = (Map) responseMap.get("result");

            FormLinkCheckResponse result = new FormLinkCheckResponse();
            result.setLinked((Boolean) resultMap.get("linked"));
            result.setSheetId((String) resultMap.get("sheetId"));
            return result;

        } catch (Exception e) {
            // Log the exception if needed
            // e.printStackTrace();

            // Return a default or failed response
            FormLinkCheckResponse errorResponse = new FormLinkCheckResponse();
            errorResponse.setLinked(false);
            errorResponse.setSheetId(null);
            return errorResponse;
        }
    }

    public TriggerListResponse listTriggers(String accessToken) {
        String deploymentScript = "AKfycbzNuGvP8n93ChY7faNMJaXfR0NJkP_Rf-vFHTOoGIvpblMI022a5uMOcplWILo1tBry9Q";
        String scriptUrl = "https://script.googleapis.com/v1/scripts/" + deploymentScript + ":run";
        RestTemplate restTemplate = new RestTemplate();

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // Body
        Map<String, Object> body = new HashMap<>();
        body.put("function", "listTriggers");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            // Call Apps Script API
            ResponseEntity<Map> response = restTemplate.exchange(scriptUrl, HttpMethod.POST, entity, Map.class);

            Map responseBody = response.getBody();
            if (responseBody == null || responseBody.get("response") == null) {
                throw new RuntimeException("No 'response' found in script API response");
            }

            Map responseContent = (Map) responseBody.get("response");
            if (responseContent.get("result") == null) {
                throw new RuntimeException("No 'result' found in script API response");
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) responseContent.get("result");

            List<TriggerInfo> triggerList = new ArrayList<>();
            for (Map<String, Object> triggerMap : results) {
                TriggerInfo trigger = new TriggerInfo();
                trigger.setFunctionName((String) triggerMap.get("functionName"));
                trigger.setTriggerId((String) triggerMap.get("triggerId"));
                trigger.setEventType((String) triggerMap.get("eventType"));
                trigger.setTriggerSource((String) triggerMap.get("triggerSource"));
                triggerList.add(trigger);
            }

            TriggerListResponse triggerListResponse = new TriggerListResponse();
            triggerListResponse.setTriggers(triggerList);
            return triggerListResponse;

        } catch (Exception e) {
            // Handle the error response
            TriggerListResponse errorResponse = new TriggerListResponse();
            errorResponse.setTriggers(new ArrayList<>()); // empty list
            // You can also log the error here: e.getMessage()
            return errorResponse;
        }
    }

    public List<CreateProject> getCreateScriptProject() {
        Long orgId = Util.getOrgIdFromToken();
        return  createProjectPpiRepository.findByOrgId(orgId);
    }
    @Transactional
    public Map<String, Object> handleFormEnableRequest(FormEnableRequest request) {
        Long orgId = Util.getOrgIdFromToken();
        Map<String, Object> response = new HashMap<>();
        FormOperation operation = request.getOperation();
        String formId = request.getFormId();
        String formType = request.getFormType();
        Boolean isFormEnabled = request.getIsFormEnabled();
        String responderUrl = request.getResponderUrl();
        String formName=request.getFormName();
        try {
            Organization org = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found with orgId: " + orgId));

            switch (operation) {
                case CHECK_EXISTENCE:
                    handleCheckExistence(org, formType, responderUrl, response, orgId,formName,formId);
//                    var optData = partnerProgramDNSDataRepository.findByOrganizationId(orgId);
//                    if (optData.isPresent()) {
//                        var partnerProgramDNSData = optData.get();
//                        partnerProgramDNSData.setFormId(formId);
//                        var save = partnerProgramDNSDataRepository.save(partnerProgramDNSData);
//                    }
                    break;

                case STATUS_UPDATE:
                    handleStatusUpdate(org, formType, isFormEnabled, responderUrl, orgId,formName,formId);
                    response.put("message", "Form status updated successfully");
                    response.put("success", true);
                    break;

                default:
                    response.put("success", false);
                    response.put("message", "Invalid operation: " + operation);
                    break;
            }

            organizationRepository.save(org);

        } catch (Exception e) {
            log.error("Error handling form enable request for orgId {}: {}", orgId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        response.put("formId", formId);
        response.put("formType", formType);
        response.put("formEnabled", isFormEnabled);
        response.put("operation", operation);
        response.put("timestamp", LocalDateTime.now());

        return response;
    }
    private void handleCheckExistence(Organization org, String formType, String responderUrl,
                                      Map<String, Object> response, Long orgId,String formName,String formKey) {
        if ("google".equalsIgnoreCase(formType)) {
            if (org.isInternalFormActive()) {
                response.put("message", "Internal form already exists");
            } else {
                markGoogleFormActive(org, responderUrl, orgId,formName,formKey);
                response.put("message", "No form exists, Google form is now active");
            }
            response.put("otherFormTypeExists", org.isInternalFormActive());
        } else if ("internal".equalsIgnoreCase(formType)) {
            if (org.isGoogleFormActive()) {
                response.put("message", "Google form already exists");
            } else {
                markInternalFormActive(org, responderUrl,formName,formKey);
                response.put("message", "No form exists, Internal form is now active");
            }
            response.put("otherFormTypeExists", org.isGoogleFormActive());
        } else {
            throw new IllegalArgumentException("Invalid formType: " + formType);
        }

        response.put("success", true);
    }

    private void handleStatusUpdate(Organization org, String formType, Boolean isFormEnabled,
                                    String responderUrl, Long orgId,String formName,String formId) {
        if (Boolean.TRUE.equals(isFormEnabled)) {
            if ("google".equalsIgnoreCase(formType)) {
                markGoogleFormActive(org, responderUrl, orgId,formName,formId);
                org.setInternalFormActive(false);
            } else if ("internal".equalsIgnoreCase(formType)) {
                markInternalFormActive(org, responderUrl,formName,formId);
                org.setGoogleFormActive(false);
            } else {
                throw new IllegalArgumentException("Invalid formType: " + formType);
            }
        } else {
            if ("google".equalsIgnoreCase(formType)) {
                org.setGoogleFormActive(false);
            } else if ("internal".equalsIgnoreCase(formType)) {
                org.setInternalFormActive(false);
            }
        }
    }

    private void markGoogleFormActive(Organization org, String responderUrl, Long orgId,String formName,String formId) {
        org.setGoogleFormActive(true);
        org.setIsFormSubmitted(true);
        org.setFormType(FormType.GOOGLE_FORM);
        org.setResponderUrl(responderUrl);
        org.setFormName(formName);
        org.setForm(formId);
        Optional<PpiEntity> optionalPpiEntity = ppiRepository.findOneByOrganization_Id(orgId);
        if (optionalPpiEntity.isPresent()) {
            PpiEntity ppiEntity = optionalPpiEntity.get();
            ppiEntity.setIsCompleted(true);
            ppiRepository.save(ppiEntity);
            updateFormDetailsStatus(formId, GOOGLE_FORM,orgId);
        } else {
            log.error("No PPI found for orgId {} while enabling Google Form", orgId);
        }
    }

    private void markInternalFormActive(Organization org, String responderUrl,String formName,String formId) {
        org.setInternalFormActive(true);
        org.setIsFormSubmitted(true);
        org.setFormType(FormType.INTERNAL_FORM);
        org.setResponderUrl(responderUrl);
        org.setFormName(formName);
        org.setForm(formId);
        Optional<PpiEntity> optionalPpiEntity = ppiRepository.findOneByOrganization_Id(org.getId());
        if (optionalPpiEntity.isPresent()) {
            PpiEntity ppiEntity = optionalPpiEntity.get();
            ppiEntity.setIsCompleted(true);
            ppiRepository.save(ppiEntity);
            updateFormDetailsStatus(formId, INTERNAL_FORM,org.getId());
        } else {
            log.error("No PPI found for orgId {} while enabling Google Form", org.getId());
        }
    }

    @Transactional
    public void updateFormDetailsStatus(String formKey, FormType formType, Long orgId) {

        Optional<FormDetails> formDetails;

        if (formType == FormType.INTERNAL_FORM) {
            try {
                Long formId = Long.parseLong(formKey);
                formDetails = formDetailsRepository
                        .findByFormIdAndOrganizationId(formId, orgId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid form ID for internal form: " + formKey);
            }
        } else if (formType == FormType.GOOGLE_FORM) {
            formDetails = formDetailsRepository
                    .findByFormAndOrganizationId(formKey, orgId);
        } else {
            throw new RuntimeException("Unsupported form type: " + formType);
        }

        if (formDetails.isEmpty()) {
            throw new RuntimeException("FormDetails not found for formKey: " + formKey);
        }

        FormDetails form = formDetails.get();
        form.setStatus(Question_Status.ACTIVE);
        formDetailsRepository.save(form);
    }



    public ViewDetailsWrapperResponse fetchByResponseId(Long responseId1, String formType, List<Long> responseIdList) {
        ViewDetailsWrapperResponse wrapper = new ViewDetailsWrapperResponse();

        FormType type;
        try {
            type = FormType.valueOf(formType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported form type: " + formType);
        }

        if (responseIdList == null || responseIdList.isEmpty()) {
            throw new IllegalArgumentException("Response ID list cannot be empty");
        }

        List<ViewDetailsResponse> responseList = new ArrayList<>();

        switch (type) {

            case GOOGLE_FORM:
                for (Long responseId : responseIdList) {
                    WebHookResponse_Ppi res = webhookResponsePpiRepository.findById(responseId)
                            .orElseThrow(() -> new RuntimeException("Webhook response not found for id: " + responseId));

                    WebHookQuestion_Ppi ques = webhookQuestionPpiRepository.findById(res.getQuestionId())
                            .orElseThrow(() -> new RuntimeException("Webhook question not found for id: " + res.getQuestionId()));

                    ViewDetailsResponse r = new ViewDetailsResponse();
                    r.setQuestion(ques.getQuestionText());
                    r.setResponse(res.getResponseText().toString());


                        r.setFirstName("Anonymous");
                        r.setEmail("anonymous@form.com");
                        r.setMobileNo("N/A");


                    responseList.add(r);
                }
                break;

            case INTERNAL_FORM:
                for (Long responseId : responseIdList) {
                    InternalResponse_Ppi response = responsePpiRepository.findById(responseId)
                            .orElseThrow(() -> new RuntimeException("Internal response not found for id: " + responseId));

                    InternalQuestion_Ppi question = questionRepo.findById(response.getQuestionId())
                            .orElseThrow(() -> new RuntimeException("Internal question not found for id: " + response.getQuestionId()));

                    if (question.getStatus() != Question_Status.ACTIVE) {
                        continue;
                    }

                    String questionText = question.getQuestionText();

                    ViewDetailsResponse r = new ViewDetailsResponse();
                    r.setQuestion(questionText);

                    if (response.getResponseTypePpi() == ResponseType_Ppi.MCQSINGLEOPTION ||
                            response.getResponseTypePpi() == ResponseType_Ppi.MCQMULTIOPTION) {

                        List<Options> options = response.getOptions();
                        String responseText = options != null && !options.isEmpty()
                                ? options.stream()
                                .map(Options::getValue)
                                .collect(Collectors.joining(", "))
                                : "No response";

                        r.setResponse(responseText);
                    } else {
                        r.setResponse(String.join(", ",
                                Optional.ofNullable(response.getResponseText()).orElse(List.of())));
                    }

                    User user = Optional.ofNullable(response.getUserId())
                            .flatMap(userRepository::findByUserId)
                            .orElse(null);

                    if (user != null) {
                        r.setFirstName(user.getName());
                        r.setEmail(user.getEmail());
                        r.setMobileNo(user.getMobile());
                    } else {
                        r.setFirstName(response.getUsername());
                        r.setEmail(response.getEmail());
                        r.setMobileNo("N/A");
                    }

                    responseList.add(r);
                }
                break;

            default:
                throw new RuntimeException("Unsupported form type: " + formType);
        }

        wrapper.setResponseList(responseList);
        return wrapper;
    }

    public ResponseEntity<LogResponse> updateStatusRequest(StatusUpdateRequest request) {
        LogResponse logResponse = new LogResponse();
        // 1. Validate input
        if (request.getResponseId() == null || request.getStatus() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "responseId and status are required");
        }

        // 2. Fetch entity
        InternalResponse_Ppi response = responsePpiRepository.findById(request.getResponseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Response not found with ID: " + request.getResponseId()));

        // 3. Update status
        response.setFormStatus(request.getStatus());
        InternalResponse_Ppi newResponse=responsePpiRepository.save(response);

        // 4. Get user details if available
        String modifiedBy = "Unknown";
        if (request.getUserId() != null) {
            Optional<User> user = userRepository.findByUserId(request.getUserId());
            if (user.isPresent()) {
                modifiedBy = user.get().getName();
            }
        }

        // 5. Save log
        LogDetails log = new LogDetails();
        log.setResponseId(request.getResponseId());
        log.setModifiedBy(modifiedBy);

        LogDetails savedlog  = logDetailsRepository.save(log);
        logResponse.setUpdatedFormStatus(String.valueOf(newResponse.getFormStatus()));
        logResponse.setModifiedBy(modifiedBy);
        logResponse.setModifiedDate(savedlog.getCreationTimestamp());
        // 6. Return response
        return ResponseEntity.ok(logResponse);
    }

    public String fetchScript() {

        Long orgId =Util.getOrgIdFromToken();
        String webhookUrl = generateWebhookUrl(String.valueOf(orgId));
        return getString(orgId, webhookUrl);
    }


    @Transactional
    public QuestionResponse patchQuestionById(InternalQuestion_Ppi request) {
        InternalQuestion_Ppi question = questionRepo.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + request.getId()));

        if (request.getQuestionText() != null) question.setQuestionText(request.getQuestionText());
        if (request.getQuestionOrder() != null) question.setQuestionOrder(request.getQuestionOrder());
        if (request.getIsRequired() != null) question.setIsRequired(request.getIsRequired());
        if (request.getHelpText() != null) question.setHelpText(request.getHelpText());
        if (request.getResponseTypePpi() != null) question.setResponseTypePpi(request.getResponseTypePpi());
        if (request.getStatus() != null) question.setStatus(request.getStatus());

        if (request.getFormId() != null) {
            List<FormDetails> formDetails = formDetailsRepository.findByFormId(request.getFormId());
            for (FormDetails formDetail : formDetails) {
                if (formDetail != null) {
                    question.setFormDetails(formDetail);
                }
            }
        }

        if (request.getOptions() != null) {
            if (question.getOptions() != null) {
                question.getOptions().clear();
            } else {
                question.setOptions(new ArrayList<>());
            }

            List<Options> newOptions = request.getOptions().stream().map(opt -> {
                Options option = new Options();
                option.setOptionId(opt.getOptionId());
                option.setValue(opt.getValue());
                option.setFormId(request.getFormId());
                option.setInternalQuestionPpi(question);
                return option;
            }).collect(Collectors.toList());

            question.getOptions().addAll(newOptions);
        }

        InternalQuestion_Ppi savedQuestion = questionRepo.save(question);

        QuestionResponse response = new QuestionResponse();
        response.setFormId(request.getFormId());
        response.setResponseMessage("Question and options updated successfully");
        response.setSavedQuestion(savedQuestion); // same as saveQuestion
        return response;
    }


    public ScriptDetails saveScript(ScriptRequest request) {
        Long orgId = Util.getOrgIdFromToken();
        String webhookUrl = generateWebhookUrl(String.valueOf(orgId));
        String script = getString(orgId, webhookUrl);

        // Step 1: Save or update ScriptDetails
        ScriptDetails scriptDetails = scriptDetailsRepository.findByFormIdAndSheetId(
                request.getFormId(), request.getSheetId()
        );

        if (scriptDetails == null) {
            scriptDetails = new ScriptDetails();
            scriptDetails.setFormId(request.getFormId());
            scriptDetails.setSheetId(request.getSheetId());
        }

        scriptDetails.setScriptId(request.getScriptId());
        scriptDetails.setScript(script);
        scriptDetails.setWebhookUrl(webhookUrl);
        scriptDetails.setOrgId(orgId);

        // Step 2: Fetch PpiEntity as Optional
        Optional<PpiEntity> optionalEntity = ppiRepository.findOneByOrganization_Id(orgId);
        PpiEntity ppiEntity;

        if (optionalEntity.isPresent()) {
            ppiEntity = optionalEntity.get();
        } else {
            ppiEntity = new PpiEntity();
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            ppiEntity.setOrganization(organization);
        }

        // Set flags (create or update)
        ppiEntity.setIsgoogleSheetUpdated(true);
        ppiEntity.setIsScriptCodeGenerated(true);
        ppiRepository.save(ppiEntity);

        return scriptDetailsRepository.save(scriptDetails);
    }



    public List<ScriptDetails> fetchScriptByOrgId() {
      Long orgId=  Util.getOrgIdFromToken();
        return scriptDetailsRepository.findByOrgId(orgId);
    }

    public List<WebHookQuestionResponse> fetchwebhookDetails() {
        List<WebHookQuestionResponse> listQuestionResponse = new ArrayList<>();
        Long orgId = Util.getOrgIdFromToken();

        try {
            List<WebHookResponse_Ppi> webHookResponseDetails = webhookResponsePpiRepository.findByOrganization_id(orgId);

            if (webHookResponseDetails != null && !webHookResponseDetails.isEmpty()) {
                for (WebHookResponse_Ppi responsePpi : webHookResponseDetails) {
                    Optional<WebHookQuestion_Ppi> optionalQuestionPpi = webhookQuestionPpiRepository.findById(responsePpi.getQuestionId());

                    if (optionalQuestionPpi.isPresent()) {
                        WebHookQuestion_Ppi questionPpi = optionalQuestionPpi.get();
                        WebHookQuestionResponse questionResponse = new WebHookQuestionResponse();

                        questionResponse.setQuestion(questionPpi.getQuestionText());
                        questionResponse.setResponse(responsePpi.getResponseText());
                        questionResponse.setFormId(responsePpi.getFormId());

                        if (questionPpi.getFormDetails() != null) {
                            questionResponse.setFormName(questionPpi.getFormDetails().getFormName());
                        } else {
                            questionResponse.setFormName("N/A");
                        }

                        listQuestionResponse.add(questionResponse);
                    } else {
                        // Log or handle case when question is not found
                        log.info("Question not found for ID: " + responsePpi.getQuestionId());
                    }
                }
            } else {
               log.info("No WebHookResponse_Ppi records found for orgId: " + orgId);
            }

        } catch (Exception e) {
            log.info("Error fetching webhook details: " + e.getMessage());
            e.printStackTrace();
        }

        return listQuestionResponse;
    }

    public List<?> getQuestionsByFormIdAndType(String formId, FormType formType) {
        List<?> questions;

        if (formType == FormType.INTERNAL_FORM) {
            questions = questionRepo
                    .findByFormDetails_FormIdAndStatus(
                            Long.valueOf(formId),
                            Question_Status.ACTIVE
                    );

        }
        else {
            throw new IllegalArgumentException("Unsupported form type: " + formType);
        }

        questions.forEach(this::applyAllotmentFlags);

        return questions;
    }

    public List<?> getQuestionsByFormIdAndTypeNOAuth(String formId, FormType formType) {

        List<?> questions;

        if (formType == FormType.INTERNAL_FORM) {
            questions = questionRepo
                    .findByFormDetails_FormIdAndStatus(
                            Long.valueOf(formId),
                            Question_Status.ACTIVE
                    );
        }
        else {
            throw new IllegalArgumentException("Unsupported form type: " + formType);
        }

        return questions;
    }


    private void applyAllotmentFlags(Object question) {
        var orgIdFromToken = Util.getOrgIdFromToken();
        var b = partnerTierRepository.existsByOrgId(orgIdFromToken);
        if (question instanceof InternalQuestion_Ppi internal) {
            internal.setApplicationReviewTimeAllotted(b);
            internal.setPartnerTierAllotted(false);
            internal.setDiscountAllotted(b);
        }

        if (question instanceof WebHookQuestion_Ppi webhook) {
            webhook.setApplicationReviewTimeAllotted(b);
            webhook.setPartnerTierAllotted(true);
            webhook.setDiscountAllotted(b);
        }
    }


    public Boolean getIsApplied(Long formId, Long appliedOrgId) {
        return formApplyStatusRepository.findByFormIdAndAppliedOrgId(formId, appliedOrgId)
                .map(FormApplyStatus::getIsApplied)
                .orElse(null);
    }

    public CounterStatsResponse getFormCounterStats(String formId, Long orgId) {

        List<CounterSave> orgCounters = counterSaveRepository.findByOrgIdAndFormId(orgId,formId);

        int totalClicks = orgCounters.stream()
                .mapToInt(c -> Optional.of(c.getCounterOnClick()).orElse(0))
                .sum();

        int totalSubmits = orgCounters.stream()
                .mapToInt(c -> Optional.of(c.getCounterOnSubmit()).orElse(0))
                .sum();

        return new CounterStatsResponse(totalClicks, totalSubmits);

    }


    public SharkdomApiResponse<PartnerPortalBrandingResponse> upsertBranding(
            PartnerPortalBrandingRequest request
    ) {
        Long orgId = Util.getOrgIdFromToken();

        PartnerPortalBranding branding = partnerPortalBrandingRepository
                .findByOrganizationId(orgId)
                .orElseGet(PartnerPortalBranding::new);

        // Set org ID only if new
        branding.setOrganizationId(orgId);

        // Update fields (works for both create & update)
        branding.setTitle(request.getTitle());
        branding.setDescription(request.getDescription());
        branding.setUrl(request.getUrl());
        branding.setEnabledReferralProgram(request.isEnabledReferralProgram());

        PartnerPortalBranding saved = partnerPortalBrandingRepository.save(branding);

        PartnerPortalBrandingResponse response =
                PartnerPortalBrandingResponse.builder()
                        .id(saved.getId())
                        .title(saved.getTitle())
                        .description(saved.getDescription())
                        .organizationId(saved.getOrganizationId())
                        .build();

        return new SharkdomApiResponse<>(
                true,
                branding.getId() == null
                        ? "Branding created successfully"
                        : "Branding updated successfully",
                response
        );
    }


    public SharkdomApiResponse<PartnerPortalBrandingResponse>
    getBrandingByOrgId(Long orgId) {

        return partnerPortalBrandingRepository.findByOrganizationId(orgId)
                .map(branding -> new SharkdomApiResponse<>(
                        true,
                        "Partner portal branding fetched successfully",
                        PartnerPortalBrandingResponse.builder()
                                .id(branding.getId())
                                .title(branding.getTitle())
                                .url(branding.getUrl())
                                .description(branding.getDescription())
                                .enabledReferralProgram(branding.getEnabledReferralProgram())
                                .organizationId(branding.getOrganizationId())
                                .PartnerTierAllotted(partnerTierRepository.existsByOrgId(branding.getOrganizationId()))
                                .discountAllotted(partnerTierRepository.existsByOrgId(branding.getOrganizationId()))
                                .createdDate(branding.getCreationTimestamp())
                                .applicationReviewTimeAllotted(true)
                                .build()
                ))
                .orElseGet(() -> new SharkdomApiResponse<>(
                        true,
                        "No branding found for this organization",
                        null
                ));
    }

    public SharkdomApiResponse<Boolean> checkBrandingCreatedForOrg(Long orgId) {

        boolean exists = partnerPortalBrandingRepository
                .existsByOrganizationId(orgId);

        return new SharkdomApiResponse<>(
                true,
                exists
                        ? "Partner portal branding already created"
                        : "Partner portal branding not created",
                exists
        );
    }


    @Transactional
    public QuestionResponse  saveResponseForExternalForm(BulkSaveResponseRequestForExternal request) {

        List<InternalResponse_Ppi> responsesToSave = new ArrayList<>();

        for (SingleQuestionResponse r : request.getResponses()) {
            ResponseType_Ppi type = ResponseType_Ppi.valueOf(r.getResponseTypePpi());
            InternalResponse_Ppi response = new InternalResponse_Ppi();
            response.setFormId(request.getFormId());
            response.setQuestionId(r.getQuestionId());
            response.setFormStatus(NOT_ASSIGNED);
            response.setIsExternalSubmission(true);
            response.setUsername(request.getUsername());
            response.setBrandName(request.getBrandName());
            response.setEmail(request.getEmail());
            response.setResponseTypePpi(type);

            if (type == ResponseType_Ppi.SINGLETEXT || type == ResponseType_Ppi.MULTITEXT) {
                response.setResponseText(
                        Optional.ofNullable(r.getResponseText()).orElse(new ArrayList<>())
                );
            }

            // Set options if type is MCQSINGLEOPTION or MCQMULTIOPTION
            if ((type == ResponseType_Ppi.MCQSINGLEOPTION || type == ResponseType_Ppi.MCQMULTIOPTION)
                    && r.getOptions() != null && !r.getOptions().isEmpty()) {

                List<Options> options = r.getOptions().stream().map(opt -> {
                    Options option = new Options();
                    option.setOptionId(opt.getOptionId());
                    option.setValue(opt.getValue());
                    option.setFormId(request.getFormId());
                    option.setInternalResponsePpi(response);
                    return option;
                }).collect(Collectors.toList());

                response.setOptions(options);
            } else {
                response.setOptions(new ArrayList<>()); // ensure non-null list
            }

            log.info("DEBUG SAVE → brand={}, user={}, email={}",
                    response.getBrandName(),
                    response.getUsername(),
                    response.getEmail()
            );

            responsesToSave.add(response);
        }

        // Save all new responses (no check for existing records)
        responsePpiRepository.saveAll(responsesToSave);
        QuestionResponse qr = new QuestionResponse();
        qr.setFormId(request.getFormId());
        qr.setResponseMessage("Responses saved successfully");

        return qr;
    }

    public SharkdomApiResponse<PartnerProgramStepper>
    upsertStepper(PartnerProgramStepperRequest request) {

        Long orgId = Util.getOrgIdFromToken();

        PartnerProgramStepper stepper = partnerProgramStepperRepository
                .findByOrganizationId(orgId)
                .orElseGet(() -> {
                    PartnerProgramStepper s = new PartnerProgramStepper();
                    s.setOrganizationId(orgId);
                    return s;
                });

        // Only update steps that are sent in request
        if (request.getStepOneCompleted() != null) {
            stepper.setStepOneCompleted(request.getStepOneCompleted());
        }

        if (request.getStepTwoCompleted() != null) {
            stepper.setStepTwoCompleted(request.getStepTwoCompleted());
        }

        if (request.getStepThreeCompleted() != null) {
            stepper.setStepThreeCompleted(request.getStepThreeCompleted());
        }

        if (request.getStepFourCompleted() != null) {
            stepper.setStepFourCompleted(request.getStepFourCompleted());
        }

        PartnerProgramStepper saved = partnerProgramStepperRepository.save(stepper);

        return new SharkdomApiResponse<>(
                true,
                "Stepper updated successfully",
                saved
        );
    }


    public SharkdomApiResponse<PartnerProgramStepper> getStepper() {
        Long orgId = Util.getOrgIdFromToken();

        log.info("Fetching partner program stepper for orgId={}", orgId);

        PartnerProgramStepper stepper = partnerProgramStepperRepository
                .findByOrganizationId(orgId)
                .orElseThrow(() -> {
                    log.warn("Stepper not found for orgId={}", orgId);
                    return new ServiceException(
                            ErrorMessages.NOT_FOUND,
                            "Stepper not found for organization"
                    );
                });

        return new SharkdomApiResponse<>(
                true,
                "Stepper fetched successfully",
                stepper
        );
    }

    public List<InternalFormResponse> postInternalFormV1(String formId, String formType) {

        List<InternalFormResponse> responseList = new ArrayList<>();
        log.info("Fetching form responses for formId: {}, formType: {}", formId, formType);

        FormType type;
        try {
            type = FormType.valueOf(formType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid formType: {}", formType, e);
            throw new IllegalArgumentException("Unsupported form type: " + formType);
        }

        try {
            Long orgId = Util.getOrgIdFromToken();
            log.info("Organization ID fetched: {}", orgId);

            Optional<Organization> organizationOpt = Optional.ofNullable(orgId)
                    .flatMap(organizationRepository::findById);

            switch (type) {

                // ===============================
                // INTERNAL FORM FLOW
                // ===============================
                case INTERNAL_FORM -> {

                    List<InternalResponse_Ppi> internalResponses =
                            responsePpiRepository.findByFormId(Long.valueOf(formId));

                    log.info("Found {} internal responses", internalResponses.size());

                    if (internalResponses.isEmpty()) {
                        log.warn("No internal responses found.");
                        return responseList;
                    }

                    // 🔥 GROUP BY USER OR EMAIL
                    Map<String, List<InternalResponse_Ppi>> grouped =
                            internalResponses.stream()
                                    .collect(Collectors.groupingBy(res -> {
                                        if (res.getUserId() != null) {
                                            return "USER_" + res.getUserId();
                                        } else {
                                            return "EMAIL_" + res.getEmail();
                                        }
                                    }));

                    for (Map.Entry<String, List<InternalResponse_Ppi>> entry : grouped.entrySet()) {

                        List<InternalResponse_Ppi> group = entry.getValue();
                        InternalResponse_Ppi first = group.get(0);

                        InternalFormResponse response = new InternalFormResponse();

                        String userId = first.getUserId();

                        // ===============================
                        // INTERNAL USER FLOW
                        // ===============================
                        if (userId != null) {

                            userRepository.findByUserId(userId)
                                    .ifPresentOrElse(user -> {

                                        response.setApplicantName(user.getName());
                                        response.setEmail(user.getEmail());

                                        organizationUserMappingRepository.findAllByUserId(userId)
                                                .stream()
                                                .findFirst()
                                                .flatMap(mapping ->
                                                        organizationRepository.findById(
                                                                mapping.getOrganizationId()
                                                        )
                                                )
                                                .ifPresentOrElse(
                                                        org -> response.setBrand(org.getName()),
                                                        () -> response.setBrand("Unknown Organization")
                                                );

                                    }, () -> {
                                        log.warn("User not found for userId: {}", userId);
                                        response.setApplicantName(null);
                                        response.setEmail(null);
                                        response.setBrand(null);
                                    });

                        }
                        // ===============================
                        // EXTERNAL USER FLOW
                        // ===============================
                        else {
                            response.setApplicantName(first.getUsername());
                            response.setEmail(first.getEmail());
                            response.setBrand(first.getBrandName());
                        }

                        // ===============================
                        // COMMON FIELDS
                        // ===============================
                        response.setIsExternalSubmission(first.getIsExternalSubmission());
                        response.setDate(first.getCreationTimestamp());

                        response.setResponseIdList(
                                group.stream()
                                        .map(InternalResponse_Ppi::getId)
                                        .collect(Collectors.toList())
                        );

                        responseList.add(response);
                    }
                }

                // ===============================
                // GOOGLE FORM FLOW
                // ===============================
                case GOOGLE_FORM -> {

                    List<WebHookResponse_Ppi> webhookResponses =
                            webhookResponsePpiRepository.findByFormId(formId);

                    log.info("Found {} webhook responses", webhookResponses.size());

                    if (organizationOpt.isEmpty() || webhookResponses.isEmpty()) {
                        log.warn("No responses or organization found.");
                        return responseList;
                    }

                    Map<String, List<WebHookResponse_Ppi>> groupedBySubmission =
                            webhookResponses.stream()
                                    .collect(Collectors.groupingBy(
                                            WebHookResponse_Ppi::getSubmissionId
                                    ));

                    for (Map.Entry<String, List<WebHookResponse_Ppi>> entry : groupedBySubmission.entrySet()) {

                        List<WebHookResponse_Ppi> group = entry.getValue();

                        InternalFormResponse anonResponse = new InternalFormResponse();
                        anonResponse.setApplicantName("Anonymous");
                        anonResponse.setEmail("anonymous@example.com");
                        anonResponse.setBrand(organizationOpt.get().getName());
                        anonResponse.setDate(group.get(0).getCreationTimestamp());

                        anonResponse.setResponseIdList(
                                group.stream()
                                        .map(WebHookResponse_Ppi::getId)
                                        .collect(Collectors.toList())
                        );

                        responseList.add(anonResponse);
                    }
                }

                default -> {
                    log.error("Unhandled form type: {}", formType);
                    throw new IllegalArgumentException("Unsupported form type: " + formType);
                }
            }

        } catch (Exception e) {
            log.error("Error fetching form responses", e);
            throw new RuntimeException("Failed to fetch form data: " + e.getMessage(), e);
        }

        log.info("Returning {} responses", responseList.size());
        return responseList;
    }

    public SharkdomApiResponse<PartnerProgramDNSData>
    getDNSDataByAzureResourceName(String azureDomainResourceName) {

        return partnerProgramDNSDataRepository
                .findByAzureDomainResourceName(azureDomainResourceName)
                .map(dns -> new SharkdomApiResponse<>(
                        true,
                        "DNS data fetched successfully",
                        dns
                ))
                .orElseGet(() -> new SharkdomApiResponse<>(
                        true,
                        "No DNS data found for given azureDomainResourceName",
                        null
                ));
    }

    public PartnerPortalBrandingResponse getBrandingByOrgIdForProfileSection(Long orgId) {

        return partnerPortalBrandingRepository.findByOrganizationId(orgId)
                .map(branding -> PartnerPortalBrandingResponse.builder()
                        .id(branding.getId())
                        .title(branding.getTitle())
                        .url(branding.getUrl())
                        .description(branding.getDescription())
                        .enabledReferralProgram(branding.getEnabledReferralProgram())
                        .organizationId(branding.getOrganizationId())
                        .PartnerTierAllotted(
                                partnerTierRepository.existsByOrgId(branding.getOrganizationId())
                        )
                        .discountAllotted(
                                partnerTierRepository.existsByOrgId(branding.getOrganizationId())
                        )
                        .createdDate(branding.getCreationTimestamp())
                        .applicationReviewTimeAllotted(true)
                        .build()
                )
                .orElse(null); // or throw exception if required
    }

}

