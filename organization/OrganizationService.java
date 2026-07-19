package com.sharkdom.service.organization;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.google.common.net.InternetDomainName;
import com.sharkdom.constants.Constants;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.organization.DocumentStatus;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.constants.organization.OrganizationStatus;
import com.sharkdom.constants.user.GenericRecordStatus;
import com.sharkdom.dto.*;
import com.sharkdom.emailOutreach.repository.EmailAccountRepository;
import com.sharkdom.entity.credits.Credits;
import com.sharkdom.entity.organization.*;
import com.sharkdom.entity.organizationcollaboration.ChannelFlag;
import com.sharkdom.entity.organizationcollaboration.OrganizationMessages;
import com.sharkdom.entity.organizationcollaboration.PartnerSpaceRoom;
import com.sharkdom.entity.organizationcollaboration.SpaceType;
import com.sharkdom.entity.ppi.PpiEntity;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.integration.model.IntegrationSaveRequest;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.organization.*;
import com.sharkdom.model.organization.GettingStartedResponse;
import com.sharkdom.offlinePartner.repository.OfflinePartnerInviteRepository;
import com.sharkdom.profilesection.service.OrganizationCertificationService;
import com.sharkdom.repository.ai.PersonaStatusRepository;
import com.sharkdom.repository.configuration.ConfigurationRepository;
import com.sharkdom.repository.email.EmailStatisticsRepository;
import com.sharkdom.repository.integration.PartnershipIntegrationRepository;
import com.sharkdom.repository.organization.*;
import com.sharkdom.repository.organizationcollaboration.OrganizationCollaborationRepository;
import com.sharkdom.repository.organizationcollaboration.OrganizationMessagesRepository;
import com.sharkdom.repository.organizationcollaboration.PartnerSpaceRepository;
import com.sharkdom.repository.partnerDeals.DealRepository;
import com.sharkdom.repository.ppi.PpiRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.configuration.ConfigurationService;
import com.sharkdom.service.credits.CreditService;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.organizationcollaboration.OrganizationCollaborationService;
import com.sharkdom.util.SharkdomApiResponse;
import com.sharkdom.util.Util;
import com.sharkdom.util.aws.service.AmazonS3Service;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sharkdom.entity.organization.Organization;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import static com.sharkdom.util.Util.getOrganizationProgress;

@Service
@Slf4j
public class OrganizationService {
    private final PreferredPartnershipTypesRepository preferredPartnershipTypesRepository;
    @Value("${setu.pan.url}")
    private String setuUrl;
    @Value("${setu.pan.client_id}")
    private String setuClientId;
    @Value("${setu.pan.secret}")
    private String setuSecret;
    @Value("${setu.pan.instance_id}")
    private String setuInstanceId;
    @Value("${logo.s3Path}")
    private String logoS3Path;
    @Value("${env}")
    private String env;
    private final OrganizationRepository organizationRepository;
    private final ObjectMapper objectMapper;
    private final OrganizationCollaborationService organizationCollaborationService;
    private final AmazonS3Service amazonS3Service;
    private final IntegrationRepository integrationRepository;
    private final OrgDocumentRepository orgDocumentRepository;
    private final OrganizationCollaborationRepository organizationCollaborationRepository;
    private final OrganizationMessagesRepository organizationMessagesRepository;
    private final PilotProgramRepository pilotProgramRepository;
    private final BookmarkOrganizationRepository bookmarkOrganizationRepository;
    private final PersonaStatusRepository personaStatusRepository;
    private final OrganizationUserMappingRepository organizationUserMappingRepository;
    private final GettingStartedRepository gettingStartedRepository;
    private final OrganizationAvailabilityRepository organizationAvailabilityRepository;
    public static final String ALGORITHM = "AES";
    private final EmailService emailService;
    private final PartnerInviteRepository partnerInviteRepository;
    private final EmailStatisticsRepository emailStatisticsRepository;
    private final PartnershipIntegrationRepository partnershipIntegrationRepository;
    private final VisitorOrganizationRepository visitorOrganizationRepository;
    private final PartnerSpaceRepository partnerSpaceRepository;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final ConfigurationRepository configurationRepository;
    private final OrganizationCertificationService organizationCertificationService;
    @Autowired
    PpiRepository ppiRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private OfflinePartnerInviteRepository offlinePartnerInviteRepository;
    @Autowired
    private ShortlistingService shortlistingService;
    @Autowired
    private EmailAccountRepository emailAccountRepository;

    @Autowired
    private DealRepository dealRepository;


    public OrganizationService(OrganizationRepository organizationRepository, ObjectMapper objectMapper, OrganizationCollaborationService organizationCollaborationService, AmazonS3Service amazonS3Service, IntegrationRepository integrationRepository, OrgDocumentRepository orgDocumentRepository, OrganizationCollaborationRepository organizationCollaborationRepository, OrganizationMessagesRepository organizationMessagesRepository, PilotProgramRepository pilotProgramRepository, BookmarkOrganizationRepository bookmarkOrganizationRepository, PersonaStatusRepository personaStatusRepository, OrganizationUserMappingRepository organizationUserMappingRepository, GettingStartedRepository gettingStartedRepository, OrganizationAvailabilityRepository organizationAvailabilityRepository, EmailService emailService, PartnerInviteRepository partnerInviteRepository, EmailStatisticsRepository emailStatisticsRepository, PartnershipIntegrationRepository partnershipIntegrationRepository, VisitorOrganizationRepository visitorOrganizationRepository, PartnerSpaceRepository partnerSpaceRepository, ConfigurationService configurationService, ConfigurationRepository configurationRepository, PreferredPartnershipTypesRepository preferredPartnershipTypesRepository, OrganizationCertificationService organizationCertificationService) {

        this.organizationRepository = organizationRepository;
        this.objectMapper = objectMapper;
        this.organizationCollaborationService = organizationCollaborationService;
        this.amazonS3Service = amazonS3Service;
        this.integrationRepository = integrationRepository;
        this.orgDocumentRepository = orgDocumentRepository;
        this.organizationCollaborationRepository = organizationCollaborationRepository;
        this.organizationMessagesRepository = organizationMessagesRepository;
        this.pilotProgramRepository = pilotProgramRepository;
        this.bookmarkOrganizationRepository = bookmarkOrganizationRepository;
        this.personaStatusRepository = personaStatusRepository;
        this.organizationUserMappingRepository = organizationUserMappingRepository;
        this.gettingStartedRepository = gettingStartedRepository;
        this.organizationAvailabilityRepository = organizationAvailabilityRepository;
        this.emailService = emailService;
        this.partnerInviteRepository = partnerInviteRepository;
        this.emailStatisticsRepository = emailStatisticsRepository;
        this.partnershipIntegrationRepository = partnershipIntegrationRepository;
        this.visitorOrganizationRepository = visitorOrganizationRepository;
        this.partnerSpaceRepository = partnerSpaceRepository;
        this.configurationRepository = configurationRepository;
        this.preferredPartnershipTypesRepository = preferredPartnershipTypesRepository;
        this.organizationCertificationService = organizationCertificationService;
    }

    public Optional<Organization> findById(long id) {
        var organization = organizationRepository.findById(id);
        if (organization.isPresent()) {
            Set<Long> orgCollaborationIds = new HashSet<>();
            organizationCollaborationService.findByOrganizationIdManual(id,0, 40).stream()
                    .forEach(organizationCollaboration -> {
                        orgCollaborationIds.add(organizationCollaboration.getSenderOrganizationId());
                        orgCollaborationIds.add(organizationCollaboration.getReceiverOrganizationId());
                    });
            orgCollaborationIds.remove(id);
            List<OrganizationCollaborationResponse> organizationCollaborationResponses = new ArrayList<>();
            orgCollaborationIds.forEach(orgCollaborationId -> {
                String name = organizationRepository.findNameById(orgCollaborationId);
                var status = organizationRepository.findStatusById(orgCollaborationId);
                var logoUrl = organizationRepository.findLogoUrlById(orgCollaborationId);
                organizationCollaborationResponses.add(OrganizationCollaborationResponse.builder()
                        .organizationId(orgCollaborationId)
                        .organizationName(name)
                        .logoUrl(logoUrl)
                        .status(status).build());
            });
            organization.get().setOrganizationCollaborations(organizationCollaborationResponses);
        }
        else{
            throw new ServiceException(ErrorMessages.SH08,id);
        }
        return organization;
    }

    public Optional<Organization> findByCode(String code) {
        return organizationRepository.findByCode(code);
    }

    public boolean isCodeAvailable(String code) {
        return !organizationRepository.existsOrganizationByCode(code);
    }

    public boolean isNameAvailable(String name) {
        return !organizationRepository.existsOrganizationByName(name);
    }

    @Transactional
    public Organization update(Organization updated) throws Exception {
        findById(updated.getId());
        return organizationRepository.save(updated);
    }

    @Transactional
    public Organization create(Organization organization) {
        // Validate website and generate code
        String website = organization.getWebsite();
        if (website == null || website.isEmpty()) {
            throw new IllegalArgumentException("Website is required");
        }

        // Extract domain name for code
        String name = extractDomainName(website);
        // Check if code already exists
        if (organizationRepository.existsOrganizationByName(name)) {
            throw new ServiceException(ErrorMessages.SH12);
        }
        // Check if code already exists
        var code = name + "-" + UUID.randomUUID().toString().substring(0, 4);
        if (organizationRepository.existsByCode(code)) {
            throw new ServiceException(ErrorMessages.SH12);
        }
        organization.setName(name);
        organization.setCode(code);

        // Set default values if not provided
        if (organization.getLogoUrl() == null) {
            organization.setLogoUrl(Constants.PLACEHOLDER_LOGO);
        }
        organization.setCredits(new Credits());

        // Save organization first
        Organization savedOrganization = organizationRepository.save(organization);

//        executorService.submit(() -> {
//            try {
//                Map<String, String> automationData = callAutomationAPI(website);
//                Optional<Organization> organizationOptional = organizationRepository.findById(savedOrganization.getId());
//                if (organizationOptional.isPresent()) {
//                    Organization orgToUpdate = organizationOptional.get();
//                    updateOrganizationWithAutomationData(orgToUpdate, automationData);
//                    organizationRepository.save(orgToUpdate);
//                }
//            } catch (Exception e) {
//                log.error("Error in automation API call for organization {}: {}", savedOrganization.getId(), e.getMessage());
//            }
//        });

        return savedOrganization;
    }

    private String extractDomainName(String website) {
        try {
            URL url = new URL(website);
            String host = url.getHost();
            InternetDomainName domainName = InternetDomainName.from(host).topPrivateDomain();
            String[] parts = domainName.toString().split("\\.");
            return parts[0];
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid website URL");
        }
    }

    private void updateOrganizationWithAutomationData(Organization organization, Map<String, String> automationData) {
        organization.setBriefDescription(automationData.get("description"));
        organization.setAbout(automationData.get("oneLineDescription"));
        organization.setCompanyType(automationData.get("marketSegment"));
        organization.setStatus(OrganizationStatus.ACTIVE);
        configurationRepository.findAllByKeyAndType("PREFERRED_SECTORS", automationData.get("sector").toUpperCase())
                .stream().findFirst().ifPresent(preferredSector -> organization.setSector(preferredSector.getValue()));


        // Handle preferred partnerships
        String partnershipsStr = automationData.get("preferredPartnerships");
        if (Objects.isNull(organization.getPreferredPartnershipTypes()) && organization.getPreferredPartnershipTypes().isEmpty()) {
            if (partnershipsStr != null && !partnershipsStr.isEmpty()) {
                List<PreferredPartnershipTypes> partnershipTypes = Arrays.stream(partnershipsStr.split(","))
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .map(type -> {
                            PreferredPartnershipTypes partnershipType = new PreferredPartnershipTypes();
                            partnershipType.setArea(type);
                            return partnershipType;
                        })
                        .collect(Collectors.toList());
                organization.setPreferredPartnershipTypes(partnershipTypes);
            }
        }


        // Handle preferred sectors
        String sectorsStr = automationData.get("idealPartnerSector");
        if (Objects.isNull(organization.getPreferredSectors()) && organization.getPreferredSectors().isEmpty()) {
            if (sectorsStr != null && !sectorsStr.isEmpty()) {
                List<PreferredSector> preferredSectors = Arrays.stream(sectorsStr.split(","))
                        .map(String::trim)
                        .map(String::toUpperCase)
                        .map(sector -> configurationRepository.findAllByKeyAndType("PREFERRED_SECTORS", sector)
                                .stream()
                                .findFirst()
                                .map(sectorValue -> {
                                    PreferredSector preferredSector = new PreferredSector();
                                    preferredSector.setArea(sectorValue.getValue());
                                    return preferredSector;
                                })
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .toList();

                organization.setPreferredSectors(preferredSectors);
            }
        }
    }

    public Page<Organization> searchByPartialName(String partialName, int page, int size) {
        return organizationRepository.findAllByNameContainingIgnoreCase(partialName, PageRequest.of(page, size));

    }

    @Transactional
    public Organization patchByCode(String code, JsonPatch patch) throws Exception {
        Optional<Organization> optionalOrganization = findByCode(code);
        optionalOrganization.orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH26));
        Organization organizationPatched = applyPatchToOrganization(patch, optionalOrganization.get());
        return organizationRepository.save(organizationPatched);
    }

    public Organization patchById(long id, JsonPatch patch) throws Exception {
        Optional<Organization> optionalOrganization = findById(id);
        optionalOrganization.orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH27, id));
        Organization organizationPatched = applyPatchToOrganization(patch, optionalOrganization.get());
        return organizationRepository.save(organizationPatched);
    }

    public List<Organization> findAllByOrganizationIdIn(List<Long> organizationIdList) {
        return organizationRepository.findAllByIdIn(organizationIdList);
    }

    private Organization applyPatchToOrganization(JsonPatch patch, Organization targetOrganization) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetOrganization, JsonNode.class));
        return objectMapper.treeToValue(patched, Organization.class);
    }

    public Page<OrganizationResponse> searchOrganization(String city, String state, String stagesCommaSeparated, int inceptionYearFrom,
                                                         String sectorsCommaSeparated, boolean includeUnverified, long queryingOrganizationId, String partnershipTypesCommaSeparated, String subSectorsCommaSeparated, int size, int page) {
        List<String> stages = null == stagesCommaSeparated ? List.of() : Arrays.stream(stagesCommaSeparated.split(",")).map(e -> e.trim().toUpperCase()).toList();
        List<String> sectors = null == sectorsCommaSeparated ? List.of() : Arrays.stream(sectorsCommaSeparated.split(",")).map(e -> e.trim().toUpperCase()).toList();
        List<String> partnershipTypes = null == partnershipTypesCommaSeparated ? List.of() : Arrays.stream(partnershipTypesCommaSeparated.split(",")).map(e -> e.trim().toUpperCase()).toList();
        List<String> subSectors = null == subSectorsCommaSeparated ? List.of() : Arrays.stream(subSectorsCommaSeparated.split(",")).map(e -> e.trim().toUpperCase()).toList();
        Page<OrganizationSearchResponse> organizationSearchResponses = organizationRepository.searchOrganization(city, state, stages, stages.isEmpty(), inceptionYearFrom, sectors, sectors.isEmpty(), includeUnverified, queryingOrganizationId, OrganizationStatus.ACTIVE, partnershipTypes, partnershipTypes.isEmpty(), subSectors, subSectors.isEmpty(), PageRequest.of(page, size));
        /*int responseSize = organizationSearchResponses.getNumberOfElements();
        Page<OrganizationSearchResponse> additionalResponse = Page.empty();
        Set<Long> uniqueIds = new HashSet<>(organizationSearchResponses.stream().map(OrganizationSearchResponse::getId).toList());
        uniqueIds.add(queryingOrganizationId);
        if (responseSize < 10) {
            additionalResponse = organizationRepository.searchRandomOrganization(uniqueIds, OrganizationStatus.ACTIVE, PageRequest.of(0, 10 - responseSize));
            Stream<OrganizationSearchResponse> concat = Stream.concat(organizationSearchResponses.get(), additionalResponse.get());
            organizationSearchResponses = new PageImpl<>(concat.toList());
        }*/
        List<OrganizationResponse> organizationResponses = new ArrayList<>();
        organizationSearchResponses.get().forEach(organizationSearchResponse -> {
            OrganizationResponse organizationResponse = mapOrganizationSearchResponse(organizationSearchResponse);
            var organization = organizationRepository.findById(organizationResponse.getId()).get();
            var preferredSectors = organization.getPreferredSectors();
            var preferredPartnershipTypes = organization.getPreferredPartnershipTypes();
            var organizationServices = organization.getServices();
            organizationResponse.setPreferredSectors(preferredSectors);
            organizationResponse.setPreferredPartnershipTypes(preferredPartnershipTypes);
            organizationResponse.setServices(organizationServices);
            organizationResponses.add(organizationResponse);
        });
        return new PageImpl<>(organizationResponses, organizationSearchResponses.getPageable(), organizationSearchResponses.getTotalElements());
    }

    private OrganizationResponse mapOrganizationSearchResponse(OrganizationSearchResponse organizationSearchResponse) {
        OrganizationResponse organizationResponse = new OrganizationResponse();
        organizationResponse.setId(organizationSearchResponse.getId());
        organizationResponse.setCode(organizationSearchResponse.getCode());
        organizationResponse.setName(organizationSearchResponse.getName());
        organizationResponse.setAbout(organizationSearchResponse.getAbout());
        organizationResponse.setBriefDescription(organizationSearchResponse.getBriefDescription());
        organizationResponse.setSector(organizationSearchResponse.getSector());
        organizationResponse.setStage(organizationSearchResponse.getStage());
        organizationResponse.setCity(organizationSearchResponse.getCity());
        organizationResponse.setState(organizationSearchResponse.getState());
        organizationResponse.setVerified(organizationSearchResponse.getVerified());
        organizationResponse.setInceptionYear(organizationSearchResponse.getInceptionYear());
        organizationResponse.setTargetMarket(organizationSearchResponse.getTargetMarket());
        organizationResponse.setRating(organizationSearchResponse.getRating());
        organizationResponse.setLogoUrl(organizationSearchResponse.getLogoUrl());
        organizationResponse.setSectorType(organizationSearchResponse.getSectorType());
        organizationResponse.setCompanyType(organizationSearchResponse.getCompanyType());
        organizationResponse.setAcknowledgmentTime(organizationSearchResponse.getAcknowledgmentTime());
        organizationResponse.setActivePartnerships(organizationSearchResponse.getActivePartnerships());
        organizationResponse.setPipelinePartnerships(organizationSearchResponse.getPipelinePartnerships());
        return organizationResponse;
    }

    @Transactional
    public VerificationResponse verifyOrganization(VerificationRequest verificationRequest) {
        var organization = organizationRepository.findById(verificationRequest.getOrganizationId());
        if (organization.isPresent()) {
            String response = verifyPan(verificationRequest.getPan());
            if (response != null) {
                organization.get().setLegalName(response);
                organization.get().setVerifiedBy("API");
                organization.get().setVerified(true);
                organization.get().setVerifiedOn(Date.from(Instant.now()));
                organizationRepository.save(organization.get());
                return VerificationResponse.builder().verified(true).build();
            }
        }
        return VerificationResponse.builder().verified(false).build();
    }

    public String verifyPan(String pan) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-client-id", setuClientId);
        headers.add("x-client-secret", setuSecret);
        headers.add("x-product-instance-id", setuInstanceId);

        PanVerificationRequest requestModel = PanVerificationRequest.builder()
                .pan(pan)
                .consent("Y")
                .reason("for verifying startup")
                .build();

        HttpEntity<PanVerificationRequest> requestEntity = new HttpEntity<>(requestModel, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> responseEntity = restTemplate.exchange(setuUrl + "/api/verify/pan", HttpMethod.POST, requestEntity, Map.class);

        // Handle the response as needed
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            try {

                var response = responseEntity.getBody();
                assert response != null;
                log.info(" response status: {} response: {}", responseEntity.getStatusCode(), responseEntity.getBody());
                if ("SUCCESS".equalsIgnoreCase(response.get("verification").toString())) {
                    Map data = (Map) responseEntity.getBody().get("data");
                    if ("company".equalsIgnoreCase(data.get("category").toString())) {
                        return data.get("full_name").toString();
                    }
                }
            } catch (Exception e) {
                log.error("exception occurred status: {} response: {} message: {}", responseEntity.getStatusCode(), responseEntity.getBody(), e.getMessage());
            }
        } else {
            log.error("bad request status: {} response: {}", responseEntity.getStatusCode(), responseEntity.getBody());
        }
        return null;

    }

    public Page<OrganizationResponse> searchPartialOrganization(String city, String state, String stagesCommaSeparated,
                                                                int inceptionYearFrom, String sectorsCommaSeparated, boolean includeUnverified, long queryingOrganizationId,
                                                                String partnershipTypesCommaSeparated, int size, int page, String partialName, String subSectorsCommaSeparated,
                                                                String companyTypesCommaSeparated, boolean exactMatch) {

        // Process search parameters
        SearchParameters params = processSearchParameters(stagesCommaSeparated, sectorsCommaSeparated,
                partnershipTypesCommaSeparated, subSectorsCommaSeparated, companyTypesCommaSeparated);

        // Get collaborations
        List<Long> collaborations = new ArrayList<>(organizationCollaborationRepository
                .getAllCollaborations(queryingOrganizationId));
        collaborations.add(queryingOrganizationId);

        // Get initial search results
        List<OrganizationSearchResponse> searchResults = organizationRepository.searchPartialOrganization(
                city, state, params.stages, params.stages.isEmpty(), inceptionYearFrom,
                params.sectors, params.sectors.isEmpty(), includeUnverified, collaborations,
                OrganizationStatus.ACTIVE, params.partnershipTypes, params.partnershipTypes.isEmpty(),
                partialName, params.subSectors, params.subSectors.isEmpty(),
                params.companyTypes, params.companyTypes.isEmpty());

        // Supplement results if needed
        if (exactMatch) {
            sortResultsByLogo(searchResults);

            // Handle pagination
            return createPaginatedResponse(searchResults, page, size);
        } else {
            List<OrganizationSearchResponse> finalResults = supplementSearchResults(searchResults,
                    new HashSet<>(collaborations), queryingOrganizationId);
            sortResultsByLogo(finalResults);

            // Handle pagination
            return createPaginatedResponse(finalResults, page, size);
        }
    }


    public Page<OrganizationResponse> searchPartialOrganizations(
            String filtersCommaSeparated,
            String sectorsCommaSeparated,
            String partnershipTypesCommaSeparated,
            int size, int page, String subSectorsCommaSeparated) {

        // Process search parameters
        SearchParameter params = processSearchParameter(filtersCommaSeparated, sectorsCommaSeparated,
                partnershipTypesCommaSeparated, subSectorsCommaSeparated);

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdatedTimestamp").descending());

        // Fetch with pagination
        Page<OrganizationSearchResponse> searchResults = organizationRepository.searchPartialOrganizations(
                params.filters, params.filters.isEmpty(),
                params.sectors, params.sectors.isEmpty(),
                params.partnershipTypes, params.partnershipTypes.isEmpty(),
                params.subSectors, params.subSectors.isEmpty(),
                pageable);

        // Convert projection into DTO if required
        return searchResults.map(this::convertToResponse);
    }

    private OrganizationResponse convertToResponse(OrganizationSearchResponse p) {
        return OrganizationResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .name(p.getName())
                .about(p.getAbout())
                .briefDescription(p.getBriefDescription())
                .sector(p.getSector())
                .stage(p.getStage())
                .city(p.getCity())
                .state(p.getState())
                .verified(p.getVerified())
                .inceptionYear(p.getInceptionYear())
                .targetMarket(p.getTargetMarket())
                .rating(p.getRating())
                .logoUrl(p.getLogoUrl())
                .companyType(p.getCompanyType())
                .acknowledgmentTime(p.getAcknowledgmentTime())
                .activePartnerships(p.getActivePartnerships())
                .pipelinePartnerships(p.getPipelinePartnerships())
                .sectorType(p.getSectorType())
                // these lists are not in the projection, so we initialize them as empty
                .preferredSectors(List.of())
                .preferredPartnershipTypes(List.of())
                .services(List.of())
                .build();
    }



    public VisitorOrganization markOrganizationVisited(Long organizationId, Long visitorId) {
        return visitorOrganizationRepository.save(VisitorOrganization.builder().organizationId(organizationId).visitorId(visitorId).build());
    }

    public List<OrganizationSearchResponse> searchPartialOrganizationV2(String subSector, String partnershipTypes, String sector) {
        List<String> subSectors = processCommaSeparatedField(subSector, true);
        List<String> partnerShipTypes = processCommaSeparatedField(partnershipTypes, true);
        List<String> sectors = processCommaSeparatedField(sector, true);
        Map<Long, String> mappedResults = getAreaMap(Util.getOrgIdFromToken());
        if (subSectors.isEmpty() && partnerShipTypes.isEmpty() && sectors.isEmpty()) {
            return topNMatches(mappedResults);
        }
        Map<Long, Long> mergedMap = new HashMap<>();
        mergeValues(mergedMap, mappedResults, subSectors);
        mergeValues(mergedMap, mappedResults, partnerShipTypes);
        mergeValues(mergedMap, mappedResults, sectors);
        Map<Long, Long> sortedMap = mergedMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        List<OrganizationSearchResponse> organizations = topNMatches(sortedMap);
        return organizations;
    }

    private List<OrganizationSearchResponse> topNMatches(Map<Long, ?> sortedMap) {
        log.info("topNMatches size: {}", sortedMap.toString());
        log.info("topNMatches: {}", sortedMap.keySet().toString());
        return sortedMap.isEmpty() ? new ArrayList<>() : organizationRepository.findOrganizationById(sortedMap.keySet().stream().toList(), OrganizationStatus.ACTIVE);
    }

    private void mergeValues(Map<Long, Long> mergedMap, Map<Long, String> mappedResults, List<String> partnerShipTypes) {
        for (Map.Entry<Long, String> entry : mappedResults.entrySet()) {
            Long key = entry.getKey();
            String value = entry.getValue();  // e.g. "BRAND_LICENSING,CO-MARKETING,STRATEGIC"

            if (value == null || value.isBlank()) continue;

            // Split the value into individual words
            String[] tokens = value.split(",");

            for (String type : partnerShipTypes) {
                for (String token : tokens) {
                    if (token.trim().equalsIgnoreCase(type.trim())) {
                        mergedMap.put(key, mergedMap.getOrDefault(key, 0L) + 1);
                        break;
                    }
                }
            }
        }
    }


    public Map<Long, String> getAreaMap(Long organizationId) {
        List<Object[]> rawResults = preferredPartnershipTypesRepository.findAllGroupedAreasAsMap(organizationId);
        return rawResults.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (String) row[1]
        ));
    }




    private record SearchParameters(List<String> stages, List<String> sectors, List<String> partnershipTypes,
                                    List<String> subSectors, List<String> companyTypes) {
    }

    private SearchParameters processSearchParameters(String stagesCommaSeparated, String sectorsCommaSeparated,
                                                     String partnershipTypesCommaSeparated, String subSectorsCommaSeparated,
                                                     String companyTypesCommaSeparated) {

        return new SearchParameters(
                processCommaSeparatedField(stagesCommaSeparated, true),
                processCommaSeparatedField(sectorsCommaSeparated, true),
                processCommaSeparatedField(partnershipTypesCommaSeparated, true),
                processCommaSeparatedField(subSectorsCommaSeparated, true),
                processCommaSeparatedField(companyTypesCommaSeparated, false)
        );
    }

    private record SearchParameter(List<String> filters, List<String> sectors, List<String> partnershipTypes,
                                    List<String> subSectors) {
    }

    private SearchParameter processSearchParameter(String filtersCommaSeparated, String sectorsCommaSeparated,
                                                     String partnershipTypesCommaSeparated, String subSectorsCommaSeparated) {

        return new SearchParameter(
                processCommaSeparatedField(filtersCommaSeparated, false),
                processCommaSeparatedField(sectorsCommaSeparated, true),
                processCommaSeparatedField(partnershipTypesCommaSeparated, true),
                processCommaSeparatedField(subSectorsCommaSeparated, true)
        );
    }

    private List<String> processCommaSeparatedField(String commaSeparated, boolean toUpperCase) {
        if (commaSeparated == null) {
            return List.of();
        }
        return Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .map(s -> toUpperCase ? s.toUpperCase() : s)
                .toList();
    }

    private List<OrganizationSearchResponse> supplementSearchResults(List<OrganizationSearchResponse> initialResults,
                                                                     Set<Long> existingIds, long queryingOrganizationId) {

        if (initialResults.size() >= 45) {
            return new ArrayList<>(initialResults);
        }

        Set<Long> uniqueIds = new HashSet<>(initialResults.stream()
                .map(OrganizationSearchResponse::getId)
                .toList());
        uniqueIds.addAll(existingIds);
        uniqueIds.add(queryingOrganizationId);

        Page<OrganizationSearchResponse> additionalResults = organizationRepository
                .searchRandomOrganization(uniqueIds, OrganizationStatus.ACTIVE,
                        PageRequest.of(0, 45 - initialResults.size()));

        return new ArrayList<>(Stream.concat(initialResults.stream(), additionalResults.get())
                .toList());
    }

    private void sortResultsByLogo(List<OrganizationSearchResponse> results) {
        results.sort((o1, o2) -> {
            boolean o1HasDefaultLogo = Constants.PLACEHOLDER_LOGO
                    .equals(o1.getLogoUrl());
            boolean o2HasDefaultLogo = Constants.PLACEHOLDER_LOGO
                    .equals(o2.getLogoUrl());

            if (o1HasDefaultLogo == o2HasDefaultLogo) {
                return 0;
            }
            return o1HasDefaultLogo ? 1 : -1;
        });
    }

    private Page<OrganizationResponse> createPaginatedResponse(List<OrganizationSearchResponse> results,
                                                               int page, int size) {

        if (results.isEmpty()) {
            return new PageImpl<>(List.of());
        }

        List<List<OrganizationSearchResponse>> partitionedResults = ListUtils.partition(results, size);

        if (page >= partitionedResults.size()) {
            return new PageImpl<>(List.of());
        }

        List<OrganizationResponse> organizationResponses = partitionedResults.get(page).stream()
                .map(this::enrichOrganizationResponse)
                .toList();

        return new PageImpl<>(organizationResponses, PageRequest.of(page, size), results.size());
    }

    private OrganizationResponse enrichOrganizationResponse(OrganizationSearchResponse searchResponse) {
        OrganizationResponse response = mapOrganizationSearchResponse(searchResponse);
        Organization organization = organizationRepository.findById(response.getId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        response.setPreferredSectors(organization.getPreferredSectors());
        response.setPreferredPartnershipTypes(organization.getPreferredPartnershipTypes());
        response.setServices(organization.getServices());

        return response;
    }


    public long getDifferenceInDays(Date startDate, Date endDate) {
        if (startDate.compareTo(endDate) == 0) {
            return 48;
        }
        LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        long differenceInDays = ChronoUnit.DAYS.between(startLocalDate, endLocalDate);
        if (differenceInDays >= 0 && differenceInDays <= 3) {
            return 12;
        } else if (differenceInDays >= 4 && differenceInDays <= 6) {
            return 36;
        } else {
            return 48;
        }
    }

    @Transactional
    public Organization uploadLogo(MultipartFile file) {
        try {
            long organizationId = Util.getOrgIdFromToken();
            var organization = organizationRepository.findById(organizationId);
            if (organization.isPresent()) {
                var orgResponse = organization.get();
                var s3Client = amazonS3Service.getS3Instance();
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(file.getSize());
                String fileName = logoS3Path + organizationId + ".png";
                var res = s3Client.putObject(new PutObjectRequest("sharkdom.co.in", fileName, file.getInputStream(), objectMetadata));
                String objectUrl = "https://s3.ap-south-1.amazonaws.com/sharkdom.co.in/" + fileName;
                orgResponse.setLogoUrl(objectUrl);

                return organizationRepository.save(orgResponse);
            } else {
                throw new ResourceNotFoundException(ErrorMessages.SH28, organizationId);
            }
        } catch (Exception e) {
            log.error("exception occurred while uploading", e);
            throw new RuntimeException("Unable to upload logo" + e.getMessage());
        }
    }

    @Transactional
    public IntegrationDetails saveIntegrationDetails(IntegrationSaveRequest integrationDetails) {
        Long orgId = Util.getOrgIdFromToken();
        PpiEntity ppiEntity = new PpiEntity();
        IntegrationDetails integrationDetailsIns = new IntegrationDetails();
        integrationDetails = integrationDetails.setOrganizationId(Util.getOrgIdFromToken());
        if (integrationRepository.existsByOrganizationIdAndIntegrationType(integrationDetails.organizationId(), integrationDetails.integrationType())) {
            var existingIntegration = integrationRepository.findByOrganizationIdAndIntegrationType(integrationDetails.organizationId(), integrationDetails.integrationType());
            existingIntegration.setRefreshToken(integrationDetails.refreshToken());
            existingIntegration.setPublishableKey(integrationDetails.publishableKey());
            existingIntegration.setConnected(true);

            integrationDetailsIns = integrationRepository.save(existingIntegration);

        }
        integrationDetailsIns = integrationRepository.save(IntegrationDetails.builder()
                .connectedId(integrationDetails.connectedId())
                .organizationId(integrationDetails.organizationId())
                .integrationType(integrationDetails.integrationType())
                .userId(integrationDetails.userId())
                .publishableKey(integrationDetails.publishableKey())
                .isConnected(true)
                .refreshToken(integrationDetails.refreshToken())
                .build());
        IntegrationType type = integrationDetails.integrationType();

// Fetch existing PpiEntity if present
        Optional<PpiEntity> optionalEntity = ppiRepository.findOneByOrganization_Id(orgId);
         ppiEntity = optionalEntity.orElseGet(PpiEntity::new);

// Set organization if creating for the first time
        if (ppiEntity.getId() == null) {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            ppiEntity.setOrganization(organization);
        }

// Set the correct integration flags
        if (type == IntegrationType.G_SHEET) {
            ppiEntity.setGoogleSheetConnected(true);
        } else if (type == IntegrationType.G_FORM) {
            ppiEntity.setGoogleFormConnected(true);
        }

        ppiRepository.save(ppiEntity);
        return integrationDetailsIns;
    }

//    @Transactional
//    public List<IntegrationDetails> getIntegrationDetails() {
//        Long organizationId = Util.getOrgIdFromToken();
//        var data = integrationRepository.findAllByOrganizationId(organizationId);
//        data.forEach(integrationDetails -> {
//            integrationDetails.setConnected(!Objects.isNull(integrationDetails.getRefreshToken()));
//        });
//        return data;
//    }

    @Transactional
    public List<IntegrationDetails> getIntegrationDetails() {

        Long organizationId = Util.getOrgIdFromToken();

        var data = integrationRepository.findAllByOrganizationSorted(organizationId);

        data.forEach(integrationDetails -> {
            integrationDetails.setConnected(
                    integrationDetails.getRefreshToken() != null
            );
        });

        return data;
    }

    @Transactional
    public IntegrationDetails saveIntegrationDetailsV1(IntegrationSaveRequest integrationDetails) {
        Long orgId = Util.getOrgIdFromToken();
        integrationDetails = integrationDetails.setOrganizationId(orgId);

        IntegrationDetails integrationDetailsIns;

        if (integrationRepository.existsByOrganizationIdAndIntegrationType(orgId, integrationDetails.integrationType())) {
            // Update existing integration
            var existingIntegration = integrationRepository.findByOrganizationIdAndIntegrationType(
                    orgId, integrationDetails.integrationType()
            );
            existingIntegration.setRefreshToken(integrationDetails.refreshToken());
            existingIntegration.setPublishableKey(integrationDetails.publishableKey());
            existingIntegration.setConnected(true);
            existingIntegration.setUserId(integrationDetails.userId());

            integrationDetailsIns = integrationRepository.save(existingIntegration);

        } else {
            // Create new integration
            integrationDetailsIns = integrationRepository.save(IntegrationDetails.builder()
                    .connectedId(integrationDetails.connectedId())
                    .organizationId(orgId)
                    .integrationType(integrationDetails.integrationType())
                    .userId(integrationDetails.userId())
                    .publishableKey(integrationDetails.publishableKey())
                    .isConnected(true)
                    .refreshToken(integrationDetails.refreshToken())
                    .build());
        }

        // === PpiEntity Handling ===
        Optional<PpiEntity> optionalEntity = ppiRepository.findOneByOrganization_Id(orgId);
        PpiEntity ppiEntity = optionalEntity.orElseGet(PpiEntity::new);

        if (ppiEntity.getId() == null) {
            Organization organization = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Organization not found"));
            ppiEntity.setOrganization(organization);
        }

        IntegrationType type = integrationDetails.integrationType();
        if (type == IntegrationType.G_SHEET) {
            ppiEntity.setGoogleSheetConnected(true);
        } else if (type == IntegrationType.G_FORM) {
            ppiEntity.setGoogleFormConnected(true);
        }

        ppiRepository.save(ppiEntity);
        return integrationDetailsIns;
    }


    @Transactional
    public OrgDocumentsEntity uploadDocument(String documentType, MultipartFile file) {
        try {
            Long organizationId = Util.getOrgIdFromToken();
            if (organizationRepository.existsOrganizationById(organizationId)) {
                var s3Client = amazonS3Service.getS3Instance();
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(file.getSize());

                String fileName;
                if (env.equalsIgnoreCase("dev")) {
                    fileName = "org/dev/docs/" + organizationId + "_" + file.getOriginalFilename();
                } else {
                    fileName = "org/prod/docs/" + organizationId + "_" + file.getOriginalFilename();
                }
                var res = s3Client.putObject(new PutObjectRequest("sharkdom.co.in", fileName, file.getInputStream(), objectMetadata));
                String objectUrl = "https://s3.ap-south-1.amazonaws.com/sharkdom.co.in/" + fileName;
                OrgDocumentsEntity orgDocument = OrgDocumentsEntity.builder()
                        .documentType(documentType)
                        .organizationId(organizationId)
                        .docUrl(objectUrl)
                        .remarks("")
                        .status(DocumentStatus.UNDER_REVIEW)
                        .build();
                return orgDocumentRepository.save(orgDocument);
            } else {
                throw new ResourceNotFoundException(ErrorMessages.SH29, organizationId);
            }
        } catch (Exception e) {
            log.error("exception occurred while uploading", e);
            throw new RuntimeException("Unable to upload file" + e.getMessage());
        }
    }

    @Transactional
    public List<OrgDocumentsEntity> getOrganizationDocuments() {
        Long organizationId = Util.getOrgIdFromToken();
        return orgDocumentRepository.findAllByOrganizationId(organizationId);
    }

    public Mono<OrgDocumentsEntity> patchDocument(long id, JsonPatch patch) throws Exception {
        return Mono.fromCallable(() -> {
            Optional<OrgDocumentsEntity> documentsEntity = orgDocumentRepository.findById(id);
            documentsEntity.orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH29, id));
            OrgDocumentsEntity patchToDocument = applyPatchToDocument(patch, documentsEntity.get());
            return orgDocumentRepository.save(patchToDocument);
        });
    }

    private OrgDocumentsEntity applyPatchToDocument(JsonPatch patch, OrgDocumentsEntity targetDocument) throws
            JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetDocument, JsonNode.class));
        return objectMapper.treeToValue(patched, OrgDocumentsEntity.class);
    }

    public Mono<List<OrgData>> findAllFromTo(String from, String to) {
        return Mono.fromCallable(() -> {
            LocalDate fromDate = LocalDate.parse(from);
            LocalDate toDate = LocalDate.parse(to);
            return organizationRepository.findAllFromTo(fromDate, toDate);
        });
    }

    public List<Map<String, Object>> getChatRoomSummaryForOrg() {
        Long orgId = Util.getOrgIdFromToken();
        List<Map<String, Object>> chatRoomSummaries = new ArrayList<>();

        // Fetch unread message counts and last messages
        List<Object[]> unreadCounts = organizationMessagesRepository.countUnreadMessagesForOrg(orgId)
                .stream()
                .filter(result -> result[0] != null)
                .toList();
        List<OrganizationMessages> lastMessages = organizationMessagesRepository.findLastMessagesForOrg(orgId)
                .stream()
                .filter(msg -> msg.getChatRoomId() != null)
                .toList();

        // Create a map for quick lookup of unread counts by chatRoomId
        Map<Long, Long> unreadCountMap = unreadCounts.stream()
                .collect(Collectors.toMap(result -> (Long) result[0], result -> (Long) result[1]));

        // Iterate over the last messages and build summaries
        for (OrganizationMessages message : lastMessages) {

            if (message.getChannelFlag() == null) {
                message.setChannelFlag(ChannelFlag.GENERAL);
            }

            Long chatRoomId = message.getChatRoomId();

            PartnerSpaceRoom partnerSpace = partnerSpaceRepository.findByChatRoomId(chatRoomId)
                    .orElseGet(() -> {
                        if (message.getSenderId() == null || message.getReceiverId() == null) {
                            throw new IllegalStateException("Invalid message with null participant Id");
                        }
                        return createAutoPartnerSpace(message);
                    });

            Map<String, Object> chatRoomSummary = new HashMap<>();
            chatRoomSummary.put("chatRoomId", chatRoomId);
            chatRoomSummary.put("spaceName", partnerSpace.getSpaceName());
            chatRoomSummary.put("partnerCreated", partnerSpace.getPartnerCreated());

            Long otherOrgId = message.getSenderId().equals(orgId) ?
                    message.getReceiverId() : message.getSenderId();

            String organizationName = organizationRepository.findNameById(otherOrgId);
            chatRoomSummary.put("organizationName", organizationName != null ? organizationName : "Unknown organization");

            chatRoomSummary.put("unreadCount", unreadCountMap.getOrDefault(chatRoomId, 0L));

            Long partnerCreated = partnerSpace.getPartnerCreated();
            String currentUserRole = (partnerCreated != null && partnerCreated.equals(orgId)) ? "ADMIN" : "MEMBER";
            chatRoomSummary.put("currentUserRole", currentUserRole);

            chatRoomSummary.put("channelFlag", message.getChannelFlag());

            List<OrganizationMessages> allMessages = organizationMessagesRepository.findAllByChatRoomIdOrderByCreationTimestampAsc(chatRoomId);
            chatRoomSummary.put("totalMessageCount", allMessages.size());

            int totalMembersCount = partnerSpace.getTotalMembers() != null ? partnerSpace.getTotalMembers() : 0;
            chatRoomSummary.put("totalMemberCount", totalMembersCount);

            chatRoomSummaries.add(chatRoomSummary);
        }
        return chatRoomSummaries;
    }

    private PartnerSpaceRoom createAutoPartnerSpace(OrganizationMessages messages) {
        PartnerSpaceRoom newSpaces = PartnerSpaceRoom.builder()
                .spaceName("Auto-created Space")
                .spaceType(SpaceType.OTHER)
                .partnerCreated(messages.getSenderId())
                .partnerCreated(messages.getReceiverId())
                .chatRoomId(messages.getChatRoomId())
                .build();

        return partnerSpaceRepository.save(newSpaces);
    }

    public void markMessageAsRead(Long messageId) {
        OrganizationMessages message = organizationMessagesRepository.findById(messageId).orElseThrow(() -> new EntityNotFoundException("Message not found"));
        message.setRead(true);
        message.setReadAt(LocalDateTime.now());
        organizationMessagesRepository.save(message);
    }

    public PilotProgram savePilotProgram(Long organizationId, PilotProgram pilotProgram) {
        if (organizationRepository.existsOrganizationById(organizationId)) {
            var organization = organizationRepository.findById(organizationId);
            organization.get().setOpenPilotProgram(true);
            pilotProgram.setOrganizationId(organizationId);
            organizationRepository.save(organization.get());
            return pilotProgramRepository.save(pilotProgram);
        } else {
            throw new ResourceNotFoundException(ErrorMessages.SH28, organizationId);
        }
    }

    public List<PilotProgram> findAllPilotProgram() {
        Long organizationId = Util.getOrgIdFromToken();
        return pilotProgramRepository.findAllByOrganizationId(organizationId);
    }

    public IntegrationDetails updateIntegration(IntegrationDetails integrationDetails) {
        integrationDetails.setOrganizationId(Util.getOrgIdFromToken());
        var integration = integrationRepository.findByOrganizationIdAndIntegrationType(integrationDetails.getOrganizationId(), integrationDetails.getIntegrationType());
        integration.setRefreshToken(integrationDetails.getRefreshToken());
        if(integrationDetails.getRefreshToken()==null)
        {
            integration.setConnected(false);
        }
        return integrationRepository.save(integration);
    }

    public void bookmarkOrganization(Long organizationId, Long partnerOrganizationId) {
        var data = bookmarkOrganizationRepository.findByOrganizationId(organizationId);
        if (data.isPresent()) {
            var bookmarkedData = data.get();
            bookmarkedData.getPartnerOrganizations().add(partnerOrganizationId);
            bookmarkOrganizationRepository.save(bookmarkedData);
        } else {
            var bookmark = new BookmarkOrganization(organizationId, List.of(partnerOrganizationId));
            bookmarkOrganizationRepository.save(bookmark);
        }

    }

    public Page<OrganizationSearchResponse> getBookmarkOrganizationDetails(Long organizationId, int page, int size) {
        var data = bookmarkOrganizationRepository.findByOrganizationId(organizationId);
        if (data.isPresent()) {
            var bookmarkedData = data.get();
            return organizationRepository.getBookmarkOrganizationDetails(OrganizationStatus.ACTIVE, bookmarkedData.getPartnerOrganizations(), PageRequest.of(page, size));
        } else {
            throw new SharkdomException(ErrorMessages.SH106);
        }
    }

    public void removeBookmarkOrganization(Long organizationId, Long partnerOrganizationId) {
        var data = bookmarkOrganizationRepository.findByOrganizationId(organizationId);
        if (data.isPresent()) {
            var bookmarkedData = data.get();
            bookmarkedData.getPartnerOrganizations().remove(partnerOrganizationId);
            bookmarkOrganizationRepository.save(bookmarkedData);
        } else {
            throw new SharkdomException(ErrorMessages.SH106);
        }
    }

    public GettingStartedResponse gettingStarted() {
        Long organizationId = Util.getOrgIdFromToken();
        var organization = organizationRepository.findById(organizationId).get();
        int organizationProgress = getOrganizationProgress(organization);
        var integrationProgress = integrationRepository.existsByOrganizationIdAndIntegrationTypeAndIsConnectedTrue(organizationId, IntegrationType.G_CALENDAR);
        var mappingCount = organizationUserMappingRepository.countByOrganizationId(organizationId) - 1;
        var personaStatus = personaStatusRepository.getByOrganizationId(organizationId);
        var gettingStarted = gettingStartedRepository.findByOrganizationId(organizationId).orElse(new GettingStartedEntity(organizationId, false, 0, GettingStartedEntity.NotFiled.NOT_FILLED, GettingStartedEntity.NotFiled.NOT_FILLED, null, null, null));
        if (Objects.isNull(gettingStarted.getId())) {
            gettingStartedRepository.save(gettingStarted);
        }
        var response = new GettingStartedResponse();
        boolean orgProfileStatus = organizationProgress == 100;
        var profileCompletion = 0;
        if (orgProfileStatus) {
            profileCompletion = 20;
        }
        response.setProfileSetup(new ProgressResponse(orgProfileStatus, profileCompletion));
        var integration = 0;
        if (integrationProgress) {
            integration = 10;
        }
        response.setPreferredMeetSetup(new ProgressResponse(integrationProgress, integration));
        boolean inviteMember = mappingCount > 0;
        var mappingProgress = 0;
        if (inviteMember) {
            mappingProgress = 20;
        }
        response.setInviteMemberSetup(new ProgressResponse(inviteMember, mappingProgress));
        boolean persona = Objects.nonNull(personaStatus) && (personaStatus.getPersonaStatus().equals(PersonaStatus.COMPLETED) || personaStatus.getPersonaStatus().equals(PersonaStatus.INITIATED));

        int personaProgress = persona ? 20 : 0;
        response.setCustomerPersonaSetup(new ProgressResponse(persona, personaProgress));
        var partnerInvite = partnerInviteRepository.findAllByOrganizationId(organizationId);
        var partnerInviteStatus = !partnerInvite.isEmpty();
        var partnerInviteCompletion = 0;
        if (partnerInviteStatus) {
            partnerInviteCompletion = 20;
        }
        response.setAddPartnersSetup(new ProgressResponse(partnerInviteStatus, partnerInviteCompletion));
        response.setProposalSetup(new ProgressResponse(gettingStarted.isProposalSetupStatus(), gettingStarted.getProposalSetupProgress()));

        var pendingCollaboration = organizationCollaborationRepository.findAllPendingCollaboration(organizationId)
                .stream().map(collaboration -> new GettingStartedResponse.PendingCollaboration(collaboration.getSenderOrganizationName(), collaboration.getId(), organizationRepository.findLogoUrlById(collaboration.getSenderOrganizationId()))).toList();
        response.setPendingCollaborations(pendingCollaboration);

        var slackConnected = integrationRepository.existsByOrganizationIdAndIntegrationTypeAndIsConnectedTrue(organizationId, IntegrationType.SLACK);
        response.setSlackConnected(slackConnected);
        var proposalSent = organizationCollaborationRepository.countBySenderId(organizationId) > 0;
        response.setProposalSent(proposalSent);
        var apiListing = partnershipIntegrationRepository.findByOrganizationId(organizationId) != null;
        response.setIntegrationProgramCreated(apiListing);
        if (gettingStarted.getApiProgram() == null) {
            response.setApiProgram(GettingStartedEntity.NotFiled.NOT_FILLED);
        } else {
            response.setApiProgram(gettingStarted.getApiProgram());
        }
        if (gettingStarted.getInHouseTeam() == null) {
            response.setInHouseTeam(GettingStartedEntity.NotFiled.NOT_FILLED);
        } else {
            response.setInHouseTeam(gettingStarted.getInHouseTeam());
        }
        response.setBrandingPage(gettingStarted.getBrandingPage());
        response.setCurrentPartnerCount(gettingStarted.getCurrentPartnerCount());
        response.setActivePartnerProgram(gettingStarted.getActivePartnerProgram());
        return response;
    }


    public GettingStartedEntity patchGettingStarted(long id, JsonPatch patch) throws Exception {
        Optional<GettingStartedEntity> optionalOrganization = gettingStartedRepository.findByOrganizationId(id);
        optionalOrganization.orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH27, id));
        GettingStartedEntity organizationPatched = applyPatchToGetting(patch, optionalOrganization.get());
        return gettingStartedRepository.save(organizationPatched);
    }

    private GettingStartedEntity applyPatchToGetting(JsonPatch patch, GettingStartedEntity targetOrganization) throws
            JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetOrganization, JsonNode.class));
        return objectMapper.treeToValue(patched, GettingStartedEntity.class);
    }

    @Transactional
    public Map<String, String> invitePartner(String email, Long organizationId, String message, String name) {
        partnerInviteRepository.findByEmail(email).ifPresent(partnerInvite -> {
            throw new SharkdomException(ErrorMessages.SH107);
        });
        String userId = RandomStringUtils.random(10, true, true);
        String encode = userId + ":" + organizationId + ":" + "role" + ":" + email;
        var encodedValue = encrypt(encode);
        String url;
        if (env.equalsIgnoreCase("dev")) {
            url = "https://dev.sharkdom.com/register?utm_register=" + encodedValue;
        } else {
            url = "https://sharkdom.com/register?utm_register=" + encodedValue;
        }
        emailService.invitePartner("Partner_invite", email, url, message, organizationId);
        var partnerInvite = PartnerInvite.builder().name(name).organizationId(organizationId).email(email).status(GenericRecordStatus.PENDING).build();
        partnerInviteRepository.save(partnerInvite);
        return Map.of("signupUrl", url);
    }

    @Transactional
    public List<Map<String, String>> bulkInvitePartner(InvitePartnerRequest invitePartnerRequest) {
        return invitePartnerRequest.bulkInvite().stream().map(invite -> {
            String userId = RandomStringUtils.random(10, true, true);
            String encode = userId + ":" + invitePartnerRequest.organizationId() + ":" + "role" + ":" + invite.email();
            var encodedValue = encrypt(encode);
            String url;
            if (env.equalsIgnoreCase("dev")) {
                url = "https://dev.sharkdom.com/register?utm_register=" + encodedValue;
            } else {
                url = "https://sharkdom.com/register?utm_register=" + encodedValue;
            }
            partnerInviteRepository.findByEmail(invite.email()).ifPresent(partnerInvite -> {
                throw new SharkdomException(ErrorMessages.SH107);
            });
            emailService.invitePartner("Partner_invite", invite.email(), url, invitePartnerRequest.message(), invitePartnerRequest.organizationId());
            var partnerInvite = PartnerInvite.builder()
                    .name(invite.name())
                    .organizationId(invitePartnerRequest.organizationId())
                    .email(invite.email())
                    .status(GenericRecordStatus.PENDING)
                    .build();
            partnerInviteRepository.save(partnerInvite);
            return Map.of("signupUrl", url);
        }).collect(Collectors.toList());
    }


    public PartnerInvite patchPartnerInvite(String email, JsonPatch patch) throws Exception {
        Optional<PartnerInvite> partnerInvite = partnerInviteRepository.findByEmail(email);
        partnerInvite.orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH30, email));
        PartnerInvite organizationPatched = applyPatchToPartner(patch, partnerInvite.get());
        return partnerInviteRepository.save(organizationPatched);
    }

    public List<PartnerInvite> getPartnerInvite(Long organizationId) {
        return partnerInviteRepository.findAllByOrganizationId(organizationId);
    }

    public List<PartnerInviteResponse> getPartnerDetails() {
        Long organizationId = Util.getOrgIdFromToken();
        var partners = partnerInviteRepository.findAllByOrganizationId(organizationId);
        return partners.stream().map(partner -> {
            var optionalOrganization = organizationRepository.findByPrimaryEmail(partner.getEmail());
            var partnerInviteResponse = PartnerInviteResponse.builder();
            partnerInviteResponse.email(partner.getEmail());
            partnerInviteResponse.id(partner.getId());
            if (optionalOrganization.isPresent()) {
                var organization = optionalOrganization.get();

                partnerInviteResponse.name(organization.getName());
                partnerInviteResponse.onboarded(true);
                partnerInviteResponse.logoUrl(organization.getLogoUrl());
                partnerInviteResponse.code(organization.getCode());
            } else {
                partnerInviteResponse.name(partner.getName());
                partnerInviteResponse.logoUrl(Constants.PLACEHOLDER_LOGO);
            }
            return partnerInviteResponse.build();
        }).toList();
    }

    private PartnerInvite applyPatchToPartner(JsonPatch patch, PartnerInvite partnerInvite) throws
            JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(partnerInvite, JsonNode.class));
        return objectMapper.treeToValue(patched, PartnerInvite.class);
    }

    public String encrypt(String data) {
        try {
            String key;
            if (env.equalsIgnoreCase("dev")) {
                key = "Uz1FyvLoNnIKdGjMIRPDKccr";
            } else {
                key = "KzaFdvfoDOIFd9SMIQPDKcE1";
            }
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OrganizationAvailability saveOrganizationAvailability(OrganizationAvailability organizationAvailability) {
        Optional<OrganizationAvailability> existingData =
                organizationAvailabilityRepository.findByOrganizationId(organizationAvailability.getOrganizationId());

        // Check if data exists
        if (existingData.isPresent()) {
            // Update the existing data
            OrganizationAvailability existingAvailability = existingData.get();
            existingAvailability.setMonday(organizationAvailability.getMonday());
            existingAvailability.setTuesday(organizationAvailability.getTuesday());
            existingAvailability.setWednesday(organizationAvailability.getWednesday());
            existingAvailability.setThursday(organizationAvailability.getThursday());
            existingAvailability.setFriday(organizationAvailability.getFriday());
            existingAvailability.setSaturday(organizationAvailability.getSaturday());
            existingAvailability.setSunday(organizationAvailability.getSunday());
            return organizationAvailabilityRepository.save(existingAvailability);
        } else {
            return organizationAvailabilityRepository.save(organizationAvailability);
        }
    }

    public Optional<OrganizationAvailability> getOrganizationAvailability() {
        var organizationId = Util.getOrgIdFromToken();
        return organizationAvailabilityRepository.findByOrganizationId(organizationId);
    }

    public Organization emailUnsubscribe(String email) {
        return organizationRepository.findByPrimaryEmail(email).map(organization -> {
            organization.setEmailUnsubscribed(true);
            return organizationRepository.save(organization);
        }).orElse(null);
    }

    public PartnerInviteStatus getPartnerInviteStatus(String email, Long organizationId) {
        var list = partnerInviteRepository.findAllByOrganizationIdAndEmail(organizationId, email);
        if (list.isEmpty()) {
            throw new SharkdomException(ErrorMessages.SH108, email, organizationId);
        }
        PartnerInviteStatus partnerInviteStatus = new PartnerInviteStatus();
        partnerInviteStatus.setEmailOpened(emailStatisticsRepository.existsByEventTypeAndEnvAndEmailAndTemplateCode("Open", env, email, "Partner_invite"));
        partnerInviteStatus.setEmailClicked(emailStatisticsRepository.existsByEventTypeAndEnvAndEmailAndTemplateCode("CLick", env, email, "Partner_invite"));
        return partnerInviteStatus;
    }

    public PartnerInviteResponse getPartnerDetailsById(Long id) {
        var partners = partnerInviteRepository.findById(id);
        return partners.map(partnerInvite -> {
            var optionalOrganization = organizationRepository.findByPrimaryEmail(partnerInvite.getEmail());
            var partnerInviteResponse = PartnerInviteResponse.builder();
            partnerInviteResponse.email(partnerInvite.getEmail());
            partnerInviteResponse.id(partnerInvite.getId());
            if (optionalOrganization.isPresent()) {
                var organization = optionalOrganization.get();

                partnerInviteResponse.name(organization.getName());
                partnerInviteResponse.onboarded(true);
                if (organization.getLogoUrl() == null) {
                    partnerInviteResponse.logoUrl(Constants.PLACEHOLDER_LOGO);
                } else {
                    partnerInviteResponse.logoUrl(organization.getLogoUrl());
                }
                partnerInviteResponse.code(organization.getCode());
            } else {
                partnerInviteResponse.name(partnerInvite.getName());
            }
            return partnerInviteResponse.build();
        }).orElseThrow(() -> {
            throw new SharkdomException(ErrorMessages.SH89, id);
        });
    }

    public PublicOrganizationResponse getPublicOrganizationResponse(String code) {
        var optionalOrganization = organizationRepository.findByCode(code);
        if (optionalOrganization.isPresent()) {
            var organization = optionalOrganization.get();
            return new PublicOrganizationResponse(organization.getName(),
                    organization.getBriefDescription(),
                    organization.getRating(),
                    organization.getPreferredPartnershipTypes(),
                    organization.getId(),
                    organization.getPreferredSectors(),
                    organization.getAcknowledgmentTime(),
                    organization.getLogoUrl());
        } else {
            return null;
        }
    }

    public Page<OrganizationResponse> getOrganizations(String subSectorsCommaSeparated, Pageable pageable) {
        List<String> subSectors = null;

        if (subSectorsCommaSeparated != null && !subSectorsCommaSeparated.isBlank()) {
            subSectors = Arrays.stream(subSectorsCommaSeparated.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        Page<Organization> page = organizationRepository.findByPreferredSubSectors(subSectors, pageable);

        return page.map(this::mapToResponse);
    }

    private OrganizationResponse mapToResponse(Organization org) {
        return OrganizationResponse.builder()
                .id(org.getId())
                .code(org.getCode())
                .name(org.getName())
                .about(org.getAbout())
                .briefDescription(org.getBriefDescription())
                .sector(org.getSector())
                .stage(org.getStage())
                .city(org.getCity())
                .state(org.getState())
                .verified(org.isVerified())
                .inceptionYear(org.getInceptionYear() != null ? org.getInceptionYear() : 0)
                .targetMarket(org.getTargetMarket())
                .rating(org.getRating())
                .logoUrl(org.getLogoUrl())
                .preferredSectors(org.getPreferredSectors())
                .preferredPartnershipTypes(org.getPreferredPartnershipTypes())
                .services(org.getServices())
                .companyType(org.getCompanyType())
                .acknowledgmentTime(org.getAcknowledgmentTime())
                .activePartnerships(org.getActivePartnerships())
                .pipelinePartnerships(org.getPipelinePartnerships())
                .sectorType(org.getSectorType())
                .build();
    }

    public static Specification<Organization> hasFilters(List<String> filters, boolean matchAll) {
        return (root, query, cb) -> {
            if (filters == null || filters.isEmpty()) {
                log.debug("No filters provided -> returning all organizations");
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            for (String filter : filters) {
                if (filter == null || filter.isEmpty()) continue;
                String pattern = "%" + filter.toLowerCase() + "%";
                log.debug("Adding predicate LIKE for filter: {}", filter);
                predicates.add(cb.like(cb.lower(root.get("filters")), pattern));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            // matchAll -> AND, otherwise OR
            return matchAll ? cb.and(predicates.toArray(new Predicate[0]))
                    : cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public Page<OrganizationCustomResponse> searchOrganizationsByFilterDto(
            List<String> filters,
            boolean matchAll,
            int page,
            int size
    ) {
        log.info("searchOrganizations DTO called with filters={}, matchAll={}, page={}, size={}",
                filters, matchAll, page, size);

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.max(1, size),
                Sort.by(Sort.Direction.DESC, "id")
        );

        // MAIN SPEC
        Specification<Organization> spec = Specification
                .where(OrganizationSpecifications.hasStatus(OrganizationStatus.ACTIVE))
                .and(OrganizationSpecifications.hasValidLogoUrl());

        if (!CollectionUtils.isEmpty(filters)) {
            spec = spec.and(OrganizationSpecifications.hasPreferredPartnershipIn(filters));
        }

        Page<Organization> result = organizationRepository.findAll(spec, pageable);

        // FALLBACK: return 3 latest created orgs
        if (result.isEmpty()) {
            log.info("No organizations found. Returning 3 latest created organizations.");

            Pageable fallbackPageable = PageRequest.of(
                    0,
                    3,
                    Sort.by(Sort.Direction.DESC, "creationTimestamp")
            );

            Specification<Organization> fallbackSpec = Specification
                    .where(OrganizationSpecifications.hasStatus(OrganizationStatus.ACTIVE))
                    .and(OrganizationSpecifications.hasValidLogoUrl());

            result = organizationRepository.findAll(fallbackSpec, fallbackPageable);
        }

        // MAP TO DTO
        return result.map(this::mapToCustomResponse);
    }

    private OrganizationCustomResponse mapToCustomResponse(Organization org) {

        boolean isSelected = offlinePartnerInviteRepository
                .findByOrganizationIdAndEmail(
                        Util.getOrgIdFromToken(),
                        org.getPrimaryEmail()
                ).isPresent();

        boolean isShortlisted = shortlistingService
                .isOrganizationShortlisted(org.getId());
        var optOrg = organizationRepository.findById(Util.getOrgIdFromToken());
        return new OrganizationCustomResponse(
                org.getId(),
                org.getName(),
                org.getAbout(),
                org.getBriefDescription(),
                org.getWebsite(),
                org.getLogoUrl(),
                org.getMeetingSuccessRate(),
                org.getAcknowledgmentTime(),
                org.getActivePartnerships(),
                org.getPipelinePartnerships(),
                org.getLegalName(),
                org.getPreferredSectors() != null
                        ? org.getPreferredSectors()
                        .stream()
                        .map(PreferredSector::getArea)
                        .toList()
                        : Collections.emptyList(),
                org.getFilters(),
                org.getPrimaryEmail(),
                isSelected,
                isShortlisted,
                org.getCompliances(),
                calculateMatchScore(org,optOrg.get()),
                org.getTrustpilotRating(),
                org.getTrustpilotTotalReviews()
        );
    }

    private List<String> parseFilters(String filters) {
        if (filters == null || filters.isBlank()) return List.of();
        return Arrays.stream(filters.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();
    }



    public boolean updateUnverifiedDealStatus( boolean isUnverifiedDeal) {
        Long orgIdFromToken = Util.getOrgIdFromToken();
        Optional<Organization> optionalOrg = organizationRepository.findById(orgIdFromToken);

        if (optionalOrg.isPresent()) {
            Organization organization = optionalOrg.get();
            organization.setUnverifiedDeal(isUnverifiedDeal);
            organizationRepository.save(organization);
            return organization.isUnverifiedDeal();
        } else {
            throw new RuntimeException("Organization not found with ID: " + orgIdFromToken);
        }
    }

    @Transactional
    public List<IntegrationType> getConnectedIntegrationTypes() {
        Long organizationId = Util.getOrgIdFromToken();

        return integrationRepository.findAllByOrganizationIdAndRefreshTokenIsNotNull(organizationId)
                .stream()
                .peek(integrationDetails -> integrationDetails.setConnected(true)) // mark connected=true
                .map(IntegrationDetails::getIntegrationType)
                .toList();
    }


    public Page<Organization> searchByPartialNameV1(String partialName, int page, int size) {

        Page<Organization> orgPage =
                organizationRepository.findAllByNameContainingIgnoreCase(
                        partialName,
                        PageRequest.of(page, size)
                );

        orgPage.forEach(org -> {
            //  Set your flags here dynamically
            org.setExternalPartnerImported(offlinePartnerInviteRepository.findByOrganizationId(org.getId()).isEmpty());
            org.setEmailOutreachConsentGranted(emailAccountRepository.findByOrganizationId(org.getId()).isEmpty());
            org.setDealCreatedOrAssigned(dealRepository.findByDealerOrgId(org.getId()).isEmpty());
            org.setAnyIntegrationAdded(integrationRepository.findAllByOrganizationId(org.getId()).isEmpty());
            org.setSendProposalCount(
                    Long.valueOf(organizationCollaborationRepository
                            .getAllCollaborations(org.getId())
                            .size())
            );


        });
        return orgPage;
    }

    public Page<Organization> searchByEmailAndName(String keyword, int page, int size) {

        Page<Organization> orgPage =
                organizationRepository.findAllByNameContainingIgnoreCaseOrPrimaryEmailContainingIgnoreCase(
                        keyword,  // for name
                        keyword,  // for email
                        PageRequest.of(page, size)
                );

        orgPage.forEach(org -> {

            // Invert if your logic means "exists" instead of "missing"
            org.setExternalPartnerImported(
                    !offlinePartnerInviteRepository.findByOrganizationId(org.getId()).isEmpty()
            );

            org.setEmailOutreachConsentGranted(
                    !emailAccountRepository.findByOrganizationId(org.getId()).isEmpty()
            );

            org.setDealCreatedOrAssigned(
                    !dealRepository.findByDealerOrgId(org.getId()).isEmpty()
            );

            org.setAnyIntegrationAdded(
                    !integrationRepository.findAllByOrganizationId(org.getId()).isEmpty()
            );

            org.setSendProposalCount(
                    (long) organizationCollaborationRepository
                            .getAllCollaborations(org.getId())
                            .size()
            );

        });

        return orgPage;
    }

    public SharkdomApiResponse<OrganizationCompliancesResponse>
    updateCompliancesByOrg( UpdateOrganizationCompliancesRequest request) {
        Long orgId=Util.getOrgIdFromToken();
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() ->
                        new RuntimeException("Organization not found with id: " + orgId)
                );

        organization.setCompliances(request.getCompliances());

        Organization updatedOrg = organizationRepository.save(organization);

        OrganizationCompliancesResponse response =
                OrganizationCompliancesResponse.builder()
                        .organizationId(updatedOrg.getId())
                        .compliances(updatedOrg.getCompliances())
                        .build();

        return new SharkdomApiResponse<>(
                true,
                "Compliances updated successfully",
                response
        );
    }

    public SharkdomApiResponse<TopPartnerResponse>
    updateTopPartnerByOrgId(Long orgId, UpdateTopPartnerRequest request) {

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() ->
                        new RuntimeException("Organization not found with id: " + orgId)
                );

        organization.setTopPartner(request.getTopPartner());

        Organization updatedOrg = organizationRepository.save(organization);

        TopPartnerResponse response =
                TopPartnerResponse.builder()
                        .organizationId(updatedOrg.getId())
                        .topPartner(updatedOrg.getTopPartner())
                        .build();

        return new SharkdomApiResponse<>(
                true,
                "Top partner updated successfully",
                response
        );
    }

    public SharkdomApiResponse<MostPopularResponse>
    updateMostPopularByOrgId(Long orgId, UpdateMostPopularRequest request) {

        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() ->
                        new RuntimeException("Organization not found with id: " + orgId)
                );

        organization.setMostPopular(request.getMostPopular());

        Organization updatedOrg = organizationRepository.save(organization);

        MostPopularResponse response =
                MostPopularResponse.builder()
                        .organizationId(updatedOrg.getId())
                        .mostPopular(updatedOrg.getMostPopular())
                        .build();

        return new SharkdomApiResponse<>(
                true,
                "Most popular updated successfully",
                response
        );
    }

    public List<OrganizationUpdateView> getOrganizationsUpdatedBetween(
            Date fromDate,
            Date toDate
    ) {
        return organizationRepository.findUpdatedOrganizationsBetweenDates(fromDate, toDate);
    }

    @Transactional
    public IntegrationDetails saveIntegrationDetailsNoAuth(IntegrationSaveRequest integrationDetails) {
        String userId = integrationDetails.userId();
        integrationDetails = integrationDetails.setUserId(userId);

        IntegrationDetails integrationDetailsIns;

        if (integrationRepository.existsByUserIdAndIntegrationType(userId, integrationDetails.integrationType())) {
            // Update existing integration
            var existingIntegration = integrationRepository.findByUserIdAndIntegrationType(
                    userId, integrationDetails.integrationType()
            );
            existingIntegration.setRefreshToken(integrationDetails.refreshToken());
            existingIntegration.setPublishableKey(integrationDetails.publishableKey());
            existingIntegration.setConnected(true);
            existingIntegration.setUserId(integrationDetails.userId());

            integrationDetailsIns = integrationRepository.save(existingIntegration);

        } else {
            // Create new integration
            integrationDetailsIns = integrationRepository.save(IntegrationDetails.builder()
                    .connectedId(integrationDetails.connectedId())
                    .integrationType(integrationDetails.integrationType())
                    .userId(integrationDetails.userId())
                    .publishableKey(integrationDetails.publishableKey())
                    .isConnected(true)
                    .refreshToken(integrationDetails.refreshToken())
                    .build());
        }
        return integrationDetailsIns;
    }

    public IntegrationDetails updateIntegrationNoAuth(IntegrationDetails integrationDetails) {
        integrationDetails.setUserId(integrationDetails.getUserId());
        var integration = integrationRepository.findByUserIdAndIntegrationType(integrationDetails.getUserId(), integrationDetails.getIntegrationType());
        integration.setRefreshToken(integrationDetails.getRefreshToken());
        if(integrationDetails.getRefreshToken()==null)
        {
            integration.setConnected(false);
        }
        return integrationRepository.save(integration);
    }

    @Transactional
    public List<IntegrationDetails> getIntegrationDetailsNoAuth(String userId) {
        var data = integrationRepository.findAllByUserId(userId);
        data.forEach(integrationDetails -> {
            integrationDetails.setConnected(!Objects.isNull(integrationDetails.getRefreshToken()));
        });
        return data;
    }

    public int calculateMatchScore(Organization org1, Organization org2) {

        int score = 0;

        if (org1 == null || org2 == null) {
            return 0;
        }

        // ---------- FILTER MATCH ----------
        List<String> filters1 = org1.getFilters();
        List<String> filters2 = org2.getFilters();

        if (filters1 != null && filters2 != null && !filters1.isEmpty() && !filters2.isEmpty()) {

            Set<String> filterSet = new HashSet<>(filters1);

            for (String filter : filters2) {
                if (filterSet.contains(filter)) {
                    score += 80;
                    break;
                }
            }
        }

        // ---------- TEAM SIZE MATCH ----------
        TeamSize teamSize1 = org1.getPartnershipTeamSize();
        TeamSize teamSize2 = org2.getPartnershipTeamSize();

        if (teamSize1 != null && teamSize2 != null) {

            if (teamSize1 != TeamSize.ZERO && teamSize2 != TeamSize.ZERO) {
                score += 20;
            }
        }

        return score;
    }

    public IntegrationDetails updateZohoWebhookDetails(

            Long organizationId,

            String apiDomain

    ) {

        IntegrationDetails integration =
                integrationRepository
                        .findByOrganizationIdAndIntegrationType(

                                organizationId,

                                IntegrationType.ZOHO
                        );

        if (integration == null) {

            throw new RuntimeException(
                    "Zoho integration not found"
            );
        }

        /*
         * Update API Domain
         */
        integration.setZohoApiDomain(
                apiDomain
        );

        /*
         * Generate tenant token
         * only if missing
         */
        if (integration.getZohoTenantToken() == null
                || integration.getZohoTenantToken().isBlank()) {

            integration.setZohoTenantToken(
                    UUID.randomUUID().toString()
            );
        }

        return integrationRepository.save(integration);
    }

}



