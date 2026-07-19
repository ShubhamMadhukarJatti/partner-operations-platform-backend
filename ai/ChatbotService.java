package com.sharkdom.service.ai;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharkdom.entity.ai.ChatbotEntity;
import com.sharkdom.entity.ai.ScheduleMeetingEntity;
import com.sharkdom.entity.ai.SharkqQueryEntity;
import com.sharkdom.entity.configuration.Configuration;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.model.ai.*;
import com.sharkdom.model.organization.OrganizationResponse;
import com.sharkdom.repository.ai.*;
import com.sharkdom.repository.configuration.ConfigurationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.email.AmazonSes;
import com.sharkdom.service.organization.OrganizationService;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ChatbotService {
    private final RestTemplate restTemplate;
    private final ChatbotMessageRepository chatbotMessageRepository;
    private final ScheduleMeetingRepository scheduleMeetingRepository;
    private final AmazonSes amazonSes;
    private final OverlapRecordsRepository overlapRecordsRepository;
    private final SharkqQueryRepository sharkqQueryRepository;
    private final OrganizationService organizationService;
    private final ConfigurationRepository configurationRepository;
    private final PersonaRepository personaRepository;
    private final OrganizationRepository organisationRepository;

    public ChatbotService(ChatbotMessageRepository chatbotMessageRepository, ScheduleMeetingRepository scheduleMeetingRepository, AmazonSes amazonSes, OverlapRecordsRepository overlapRecordsRepository, SharkqQueryRepository sharkqQueryRepository, OrganizationService organizationService, ConfigurationRepository configurationRepository, PersonaRepository personaRepository, OrganizationRepository organisationRepository) {
        this.scheduleMeetingRepository = scheduleMeetingRepository;
        this.amazonSes = amazonSes;
        this.overlapRecordsRepository = overlapRecordsRepository;
        this.sharkqQueryRepository = sharkqQueryRepository;
        this.organizationService = organizationService;
        this.configurationRepository = configurationRepository;
        this.personaRepository = personaRepository;
        this.organisationRepository = organisationRepository;
        this.restTemplate = new RestTemplate();
        this.chatbotMessageRepository = chatbotMessageRepository;
    }

    @Transactional
    public Object sendMessage(ChatbotMessage chatbotMessage) {
        chatbotMessageRepository.save(ChatbotEntity.builder().userId(chatbotMessage.userId()).message(chatbotMessage.message()).build());
        return callChatbot(chatbotMessage);
    }

    @Transactional
    public ScheduleMeetingEntity scheduleMeeting(MeetingSchedule meetingSchedule) {
        try {
            amazonSes.prepareAndSend("Meeting booked from Chatbot", generateEmailContent(meetingSchedule), null, "office@sharkdom.com", String.join(",", "office@sharkdom.com"), null, "chatbot_meeting", 0L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return scheduleMeetingRepository.save(ScheduleMeetingEntity.builder().userId(meetingSchedule.userId()).company(meetingSchedule.company()).purpose(meetingSchedule.purpose()).email(meetingSchedule.email()).date(meetingSchedule.date()).time(meetingSchedule.time()).companySize(meetingSchedule.companySize()).name(meetingSchedule.name()).build());
    }

    private Object callChatbot(ChatbotMessage chatbotMessage) {
        Map<String, String> postRequest = new HashMap<>();
        postRequest.put("message", chatbotMessage.message());
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(postRequest);
        Object responseEntity = restTemplate.exchange("https://sharkdom-chatbot.azurewebsites.net/chat", HttpMethod.POST, requestEntity, Object.class).getBody();
        return responseEntity;
    }


    public String generateEmailContent(MeetingSchedule meeting) {
        String emailTemplate = """
                <!DOCTYPE html>
                <html>
                <body>
                    <h2>Meeting Schedule Details Booked from Chatbot</h2>
                    <p><strong>User ID:</strong> %s</p>
                    <p><strong>Name:</strong> %s</p>
                    <p><strong>Company:</strong> %s</p>
                    <p><strong>Company Size:</strong> %s</p>
                    <p><strong>Purpose:</strong> %s</p>
                    <p><strong>Email:</strong> %s</p>
                    <p><strong>Date:</strong> %s</p>
                    <p><strong>Time:</strong> %s</p>
                
                </body>
                </html>
                """;

        return String.format(emailTemplate, meeting.userId(), meeting.name(), meeting.company(), meeting.companySize(), meeting.purpose(), meeting.email(), meeting.date(), meeting.time());
    }

    @Transactional
    public Map<String, Object> sharkqQuery(SharkqQueryRequest query) {
        String allOrgNamesJson = null;
        Long organizationId = Util.getOrgIdFromToken();

        Optional<Organization> organizationData = organisationRepository.findById(organizationId);
        // Check prerequisites based on promptId
        if (query.getPromptId() == 1 && !overlapRecordsRepository.existsByOrganizationId(organizationId)) {
            String errorMessage = "Before we show IPP (Ideal Partner Profile) for " +
                    organizationData.map(Organization::getName).orElse("no data available for the organisation") +
                    ", need some more details?<br/>" +
                    "<strong>Please connectAccount with CRM or upload data from an XLS/CSV file first:</strong><br/>" +
                    "<ConnectProspectsModal/>";;
            saveMessages(organizationId, query.getQuery(), errorMessage, allOrgNamesJson);
            return Map.of("text", errorMessage, "prompt", query.getQuery());
        } else if (query.getPromptId() == 2) {
            String personaErrorMessage = """
                    No data available for prospects ,
                    <br/>
                    **Please connectAccount with CRM or upload a data from XLS/CSV first:**
                    <br/> 
                    <ConnectProspectsModal/>
                    """;
            saveMessages(organizationId, query.getQuery(), personaErrorMessage, allOrgNamesJson);
            return Map.of("text", personaErrorMessage, "prompt", query.getQuery());
        } else if (query.getPromptId() == 3) {
            String personaErrorMessage = """
                    No data available for opportunities ,
                    <br/>
                    **Please connectAccount with CRM or upload a data from XLS/CSV first:**
                    <br/> 
                    <ConnectOpportunityModal/>
                    """;
            saveMessages(organizationId, query.getQuery(), personaErrorMessage, allOrgNamesJson);
            return Map.of("text", personaErrorMessage, "prompt", query.getQuery());
        } else if (query.getPromptId() == 5) {
            String saveMessage = "Sure,we are not processing proposals.... ";
            saveMessages(organizationId, query.getQuery(), saveMessage, allOrgNamesJson);
            return Map.of("text", saveMessage, "prompt", query.getQuery());
        } else if (query.getPromptId() == 6) {
            String saveMessage = "Sending Proposal..";
            saveMessages(organizationId, query.getQuery(), saveMessage, allOrgNamesJson);
            return Map.of("text", saveMessage, "prompt", query.getQuery());
        }

        // Handle promptId specific logic
        // Make API call to search service
        String searchUrl = "https://sharkdom-search-cdd5a2h0ftgbfqah.centralindia-01.azurewebsites.net/query";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("input", query.getQuery());

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(searchUrl, HttpMethod.POST, requestEntity, Map.class);


        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> searchResponse = response.getBody();

            // Get values from configuration repository
            List<String> partnershipTypes = (List<String>) searchResponse.get("partnership_type");
            List<String> sectors = (List<String>) searchResponse.get("sector");

            // Get config values for sectors and partnership types
            List<String> configSectors = sectors.stream().map(sector -> configurationRepository.findAllByKeyAndType("PREFERRED_SECTORS", sector).stream().findFirst().map(Configuration::getValue).orElse(sector)).toList();

            List<String> configPartnershipTypes = partnershipTypes.stream().map(type -> configurationRepository.findAllByKeyAndType("PREFERRED_PARTNERSHIPS", type).stream().findFirst().map(Configuration::getValue).orElse(type)).toList();

            // Search organizations using the searchPartialOrganization method
            Page<OrganizationResponse> organizations = organizationService.searchPartialOrganization("", // city
                    "", // state
                    "", // stages
                    0, // inceptionYearFrom
                    String.join(",", configSectors), // sectors
                    true, // includeUnverified
                    0L, // queryingOrganizationId
                    String.join(",", configPartnershipTypes), // partnershipTypes
                    20, // size
                    0, // page
                    "", // partialName
                    "", // subSectors
                    "", // companyTypes
                    false // exactMatch
            );
            allOrgNamesJson = organizations.getContent().stream().map(org -> String.format("{\"id\": %d, \"name\": \"%s\"}", org.getId(), org.getName().replace("\"", "\\\""))).collect(Collectors.joining(", ", "[", "]"));
            // Format the response
            StringBuilder responseText = new StringBuilder();


            responseText.append("<SendProposalComponent orgNameList={" + allOrgNamesJson + "}/>");


            String finalResponseText = responseText.toString();

            saveMessages(organizationId, query.getQuery(), finalResponseText, allOrgNamesJson);

            return Map.of("text", finalResponseText, "organizations", organizations.getContent(), "prompt", query.getQuery());
        }

        // Default case - save query and return empty response
        saveMessages(organizationId, query.getQuery(), "", allOrgNamesJson);
        return Map.of("text", "", "prompt", query.getQuery());
    }

    private void saveMessages(Long organizationId, String query, String response, String allOrgNames) {
        sharkqQueryRepository.saveAll(List.of(SharkqQueryEntity.builder().message(query).prompt(query).organizationId(organizationId).isBot(false).orgNames(allOrgNames).build(), SharkqQueryEntity.builder().message(response).prompt(query).organizationId(organizationId).isBot(true).orgNames(allOrgNames).build()));
    }

    public List<SharkqQueryEntity> getSharkqQuery() {
        List<SharkqQueryEntity> sharkqQueryEntityList = new ArrayList<>();

        try {
            Long organizationId = Util.getOrgIdFromToken();
            List<SharkqQueryEntity> fetchedList = sharkqQueryRepository.findByOrganizationId(organizationId);

            ObjectMapper mapper = new ObjectMapper();

            for (SharkqQueryEntity sharkqQueryEntity : fetchedList) {

                List<Map<String, Object>> parsedOrgNames = new ArrayList<>();
                // Optional: Parse orgNames JSON string if needed
                if (sharkqQueryEntity.getOrgNames() != null) {
                    try {
                        parsedOrgNames = mapper.readValue(sharkqQueryEntity.getOrgNames(), new TypeReference<List<Map<String, Object>>>() {
                                }

                        );
                        sharkqQueryEntity.setOrgNamesList(parsedOrgNames);
//                        sharkqQueryEntity.setMessage("<SendProposalComponent orgNameList {"+ parsedOrgNames +"}/>");
                        // You can use parsedOrgNames if needed
                    } catch (Exception jsonEx) {
                        System.err.println("Failed to parse orgNames JSON for ID: " + sharkqQueryEntity.getId());
                        jsonEx.printStackTrace();
                    }
                }

                sharkqQueryEntityList.add(sharkqQueryEntity);
            }

        } catch (Exception e) {
            System.err.println("Error fetching SharkqQueryEntities:");
            e.printStackTrace();
        }

        return sharkqQueryEntityList;
    }


    public SharkqQueryEntity getSharkqQueryById(Long id) {
        ObjectMapper mapper = new ObjectMapper();
        Optional<SharkqQueryEntity> sharkqQueryEntity = sharkqQueryRepository.findById(id);
        List<Map<String, Object>> parsedOrgNames = new ArrayList<>();
        // Optional: Parse orgNames JSON string if needed
        if (sharkqQueryEntity.get().getOrgNames() != null) {
            try {
                parsedOrgNames = mapper.readValue(sharkqQueryEntity.get().getOrgNames(), new TypeReference<List<Map<String, Object>>>() {
                        }

                );
                sharkqQueryEntity.get().setOrgNamesList(parsedOrgNames);
//
//                String message = "<SendProposalComponent orgNameList {"+ parsedOrgNames +"}/>";
//                List<Map<String, Object>> orgList =   extractOrgListJson( message);
//                String json = mapper.writeValueAsString(orgList);
//                sharkqQueryEntity.get().setMessage(message);
                // You can use parsedOrgNames if needed
            } catch (Exception jsonEx) {
                System.err.println("Failed to parse orgNames JSON for ID: " + sharkqQueryEntity.get().getId());
                jsonEx.printStackTrace();
            }
        }
        return sharkqQueryEntity.get();
    }

}
