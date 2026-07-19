package com.sharkdom.service.ai;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.constants.PartnerDataFields;
import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.entity.ai.*;
import com.sharkdom.entity.organization.IntegrationDetails;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.PersonaResponse;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.ai.*;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.organizatiocollaboration.CollaborationCategory;
import com.sharkdom.model.persona.PersonaMatchDto;
import com.sharkdom.repository.ai.*;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.GoogleSheetService;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.organizationcollaboration.OrganizationCollaborationService;
import com.sharkdom.tablecustomization.service.overlaprecordfieldentityservice.OverlapRecordFieldEntityTableColumnService;
import com.sharkdom.util.CategoryComparator;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PersonaService {
    private final PersonaRepository personaRepository;
    private final PersonaDetailsRepository personaDetailsRepository;
    private final PersonaStatusRepository personaStatusRepository;
    private final EmailService emailService;
    private final PersonaNotifyRepository personaNotifyRepository;
    private final OrganizationRepository organizationRepository;
    private final ModeSaveRepository modeSaveRepository;
    private final IntegrationRepository integrationRepository;
    private final HubspotService hubspotService;
    private final GoogleSheetService googleSheetService;
    private final OverlapRecordsRepository overlapRecordsRepository;
    private final AsyncPersonaService asyncPersonaService;
    private final PersonaUserNotifyRepository personaUserNotifyRepository;
    private final PartnerDataPermissionRepository partnerDataPermissionRepository;
    private final OrganizationCollaborationService organizationCollaborationService;
    private static final String NOT_SHARED = "NOT_SHARED";
    private final IntegrationService integrationService;
    private final ExternalPartnerOverlapRecordsRepository externalPartnerOverlapRecordsRepository;
    private final OverlapRecordFieldEntityTableColumnService dynamicTableService;
    private final AsyncPersonaServiceVersioning asyncPersonaServiceVersioning;

    public PersonaService(PersonaRepository personaRepository, PersonaDetailsRepository personaDetailsRepository, PersonaStatusRepository personaStatusRepository, EmailService emailService, PersonaNotifyRepository personaNotifyRepository, OrganizationRepository organizationRepository, ModeSaveRepository modeSaveRepository, IntegrationRepository integrationRepository, HubspotService hubspotService, GoogleSheetService googleSheetService, OverlapRecordsRepository overlapRecordsRepository, AsyncPersonaService asyncPersonaService, PersonaUserNotifyRepository personaUserNotifyRepository, PartnerDataPermissionRepository partnerDataPermissionRepository, OrganizationCollaborationService organizationCollaborationService, IntegrationService integrationService, IntegrationService integrationService1, ExternalPartnerOverlapRecordsRepository externalPartnerOverlapRecordsRepository, OverlapRecordFieldEntityTableColumnService dynamicTableService, AsyncPersonaServiceVersioning asyncPersonaServiceVersioning) {
        this.personaRepository = personaRepository;
        this.personaDetailsRepository = personaDetailsRepository;
        this.personaStatusRepository = personaStatusRepository;
        this.emailService = emailService;
        this.personaNotifyRepository = personaNotifyRepository;
        this.organizationRepository = organizationRepository;
        this.modeSaveRepository = modeSaveRepository;
        this.integrationRepository = integrationRepository;
        this.hubspotService = hubspotService;
        this.googleSheetService = googleSheetService;
        this.overlapRecordsRepository = overlapRecordsRepository;
        this.asyncPersonaService = asyncPersonaService;
        this.personaUserNotifyRepository = personaUserNotifyRepository;
        this.partnerDataPermissionRepository = partnerDataPermissionRepository;
        this.organizationCollaborationService = organizationCollaborationService;
        this.integrationService = integrationService;
        this.externalPartnerOverlapRecordsRepository = externalPartnerOverlapRecordsRepository;
        this.dynamicTableService = dynamicTableService;
        this.asyncPersonaServiceVersioning = asyncPersonaServiceVersioning;
    }

    public Map<String, List<PercentageCategory>> getAllData(Long organizationId) {
        List<PersonaEntity> records = personaRepository.getAllByOrganizationId(organizationId);

        Map<String, List<PercentageCategory>> response = new HashMap<>();

        records.forEach(record -> response
                .computeIfAbsent(record.getAttribute(), k -> new java.util.ArrayList<>())
                .add(new PercentageCategory(record.getCategory(), record.getPercentage()))
        );
        return response;
    }

    public Map<String, List<PercentageCategory>> getAllData(Long organizationId, Integer versionId) {

        List<PersonaEntity> records;

        // etch based on version
        if (versionId != null) {
            records = personaRepository
                    .findByOrganizationIdAndVersionId(organizationId, versionId);
        } else {
            // Fetch latest version only (IMPORTANT FIX)
            Integer latestVersion = personaRepository
                    .findTopVersionByOrganizationId(organizationId);

            records = personaRepository
                    .findByOrganizationIdAndVersionId(organizationId, latestVersion);
        }

        Map<String, List<PercentageCategory>> response = new HashMap<>();

        records.forEach(record -> response
                .computeIfAbsent(record.getAttribute(), k -> new ArrayList<>())
                .add(new PercentageCategory(record.getCategory(), record.getPercentage()))
        );

        return response;
    }


    public PersonaResponse getPersonaDetails(int page, int size) {
        Long organizationId = Util.getOrgIdFromToken();
        var statusResponse = personaStatusRepository.getByOrganizationId(organizationId);
        var response = personaDetailsRepository.getAllByOrganizationId(organizationId, PageRequest.of(page, size));
        var details = getAllData(organizationId);

        var builder = PersonaResponse.builder().personaDetails(response).category(details);

        if (Objects.nonNull(statusResponse)) {
            builder.mode(statusResponse.getPersonaMode()).personaStatus(statusResponse.getPersonaStatus()).creationTimestamp(statusResponse.getCreationTimestamp());
        }
        var personaResponse = builder.build();
        List<PercentageCategory> industries = details.get("companySector");
        if (industries != null && !industries.isEmpty()) {
            PercentageCategory topIndustry = industries.stream()
                    .max(Comparator.comparingDouble(PercentageCategory::getPercentage))
                    .orElse(null);
            personaResponse.setTopIndustry(topIndustry.getKey());
            personaResponse.setTopIndustryPercentage(topIndustry.getPercentage());
        }
        List<PercentageCategory> sectors = details.get("marketSegment");
        if (sectors != null && !sectors.isEmpty()) {
            PercentageCategory topSector = sectors.stream()
                    .max(Comparator.comparingDouble(PercentageCategory::getPercentage))
                    .orElse(null);
            personaResponse.setTopMarketSegment(topSector.getKey());
            personaResponse.setTopMarketSegmentPercentage(topSector.getPercentage());
        }
        return personaResponse;
    }

    public PersonaResponse getPersonaDetailsV1(int page, int size, Integer versionId) {

        Long organizationId = Util.getOrgIdFromToken();

        Page<PersonaDetailsEntity> response;
        PersonaStatusEntity statusResponse;

        // ✅ If versionId passed → fetch specific version
        if (versionId != null) {
            response = personaDetailsRepository
                    .findByOrganizationIdAndVersionId(organizationId, versionId, PageRequest.of(page, size));

            statusResponse = personaStatusRepository
                    .findByOrganizationIdAndVersionId(organizationId, versionId);

        } else {
            // Default → latest version
            response = personaDetailsRepository
                    .findByOrganizationIdOrderByVersionIdDesc(organizationId, PageRequest.of(page, size));

            statusResponse = personaStatusRepository
                    .findTopByOrganizationIdOrderByVersionIdDesc(organizationId);
        }

        // 👇 IMPORTANT: pass versionId to aggregation also
        var details = getAllData(organizationId, versionId);

        var builder = PersonaResponse.builder()
                .personaDetails(response)
                .category(details);

        if (Objects.nonNull(statusResponse)) {
            builder.mode(statusResponse.getPersonaMode())
                    .personaStatus(statusResponse.getPersonaStatus())
                    .creationTimestamp(statusResponse.getCreationTimestamp());
        }

        var personaResponse = builder.build();

        //  Top Industry
        List<PercentageCategory> industries = details.get("companySector");
        if (industries != null && !industries.isEmpty()) {
            PercentageCategory topIndustry = industries.stream()
                    .max(Comparator.comparingDouble(PercentageCategory::getPercentage))
                    .orElse(null);

            if (topIndustry != null) {
                personaResponse.setTopIndustry(topIndustry.getKey());
                personaResponse.setTopIndustryPercentage(topIndustry.getPercentage());
            }
        }

        // Top Market Segment
        List<PercentageCategory> sectors = details.get("marketSegment");
        if (sectors != null && !sectors.isEmpty()) {
            PercentageCategory topSector = sectors.stream()
                    .max(Comparator.comparingDouble(PercentageCategory::getPercentage))
                    .orElse(null);

            if (topSector != null) {
                personaResponse.setTopMarketSegment(topSector.getKey());
                personaResponse.setTopMarketSegmentPercentage(topSector.getPercentage());
            }
        }

        return personaResponse;
    }

    public Map<String, String> saveDummy(CompanyDetails companyDetails) {
        Long organizationId = Util.getOrgIdFromToken();
        List<PersonaEntity> attributes = new ArrayList<>();

        // Process isPartnershipProgram
        for (PercentageCategory kp : companyDetails.getIsPartnershipProgram()) {
            attributes.add(createCompanyAttribute("isPartnershipProgram", kp, organizationId));
        }

        // Process companySector
        for (PercentageCategory kp : companyDetails.getCompanySector()) {
            attributes.add(createCompanyAttribute("companySector", kp, organizationId));
        }

        // Process companySize
        for (PercentageCategory kp : companyDetails.getCompanySize()) {
            attributes.add(createCompanyAttribute("companySize", kp, organizationId));
        }

        // Process marketSegment
        for (PercentageCategory kp : companyDetails.getMarketSegment()) {
            attributes.add(createCompanyAttribute("marketSegment", kp, organizationId));
        }

        personaRepository.saveAll(attributes);

        return Map.of("response", "details saved successfully");
    }

    private PersonaEntity createCompanyAttribute(String attributeType, PercentageCategory kp, Long companyId) {
        PersonaEntity attribute = new PersonaEntity();
        attribute.setAttribute(attributeType);
        attribute.setCategory(kp.getKey());
        attribute.setPercentage(kp.getPercentage());
        attribute.setOrganizationId(companyId);
        return attribute;
    }

    public void notifyPersona(Long senderId, Long notifyId) {
        personaNotifyRepository.save(PersonaNotifyEntity.builder().receiverOrganizationId(notifyId).senderOrganizationId(senderId).build());
        TemplateOrganizationEmailReqModel reqModel = TemplateOrganizationEmailReqModel.builder()
                .organizationName(organizationRepository.findNameById(notifyId))
                .templateCode("notify_persona")
                .organizationIds(List.of(notifyId))
                .build();
        emailService.sendByTemplateAndOrganizationIds(reqModel, null, senderId, 1L);
    }

    public List<PersonaNotifyEntity> notifyPersonaDetails() {
        return personaNotifyRepository.findAll();
    }

    public Map<String, String> saveModeDetails(ModeSaveRequest modeSaveRequest) {
        ModeSaveEntity modeSaveEntity =
                ModeSaveEntity.builder()
                        .organizationId(modeSaveRequest.organizationId())
                        .mode(modeSaveRequest.mode())
                        .entity("CUSTOMER_PERSONA")
                        .build();
        modeSaveRepository.save(modeSaveEntity);
        return Map.of("response", "details saved successfully");
    }

    public Page<ModeSaveResponse> getEntities(int page, int size, Order order) {
        Sort sort = order.equals(Order.ASCENDING)
                ? Sort.by("creationTimestamp").ascending()
                : Sort.by("creationTimestamp").descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ModeSaveEntity> data = modeSaveRepository.findByEntity("PARTNER_LISTING", pageable);

        return data.map(modeSaveEntity -> {
            var integration = personaRepository.findFirstByOrganizationId(modeSaveEntity.getOrganizationId());
            var modeSaveResponse = ModeSaveResponse.builder();

            if (integration.isPresent()) {
                modeSaveResponse.createdOn(integration.get().getCreationTimestamp()).created(true);
            } else {
                modeSaveResponse.createdOn(null).created(false);
            }

            return modeSaveResponse
                    .mode(modeSaveEntity.getMode())
                    .organizationId(modeSaveEntity.getOrganizationId())
                    .clickedOn(modeSaveEntity.getCreationTimestamp())
                    .build();
        });
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    @SuppressWarnings("unused")
    public void refreshPersona() {
        var personas = personaStatusRepository.findAll();
        LocalDate today = LocalDate.now();
        personas.forEach(personaStatusEntity -> {
            if (personaStatusEntity.getPersonaStatus().equals(PersonaStatus.COMPLETED)) {
                if (shouldExecuteToday(personaStatusEntity, today)) {
                    var hubSpotIntegration = integrationRepository.findByOrganizationIdAndIntegrationType(personaStatusEntity.getOrganizationId(), IntegrationType.HUBSPOT);
                    var sheetIntegration = integrationRepository.findByOrganizationIdAndIntegrationType(personaStatusEntity.getOrganizationId(), IntegrationType.G_SHEET);
                    if (hubSpotIntegration != null) {
                        var data = hubspotService.getDetails(personaStatusEntity.getOrganizationId(), personaStatusEntity.getColumnName());
                        if (data != null) {
                            var websites = extractWebsitesList(data);
                            PersonaRequest personaRequest = new PersonaRequest(personaStatusEntity.getOrganizationId(), websites, new String[]{}, personaStatusEntity.getFrequency(), "", personaStatusEntity.getColumnName(), personaStatusEntity.getGoogleSheetLink(), personaStatusEntity.getPersonaMode(), personaStatusEntity.getFileName());
//                            asyncPersonaService.savePersona(personaRequest);
                        }
                    } else if (sheetIntegration != null) {
                        var data = googleSheetService.getColumnValues(personaStatusEntity.getGoogleSheetLink(), personaStatusEntity.getColumnName(), sheetIntegration.getRefreshToken());
                        if (!data.isEmpty()) {
                            PersonaRequest personaRequest = new PersonaRequest(personaStatusEntity.getOrganizationId(), data, new String[]{}, personaStatusEntity.getFrequency(), "", personaStatusEntity.getColumnName(), personaStatusEntity.getGoogleSheetLink(), personaStatusEntity.getPersonaMode(), personaStatusEntity.getFileName());
//                            asyncPersonaService.savePersona(personaRequest);
                        }
                    }
                }
            }
        });
    }

    private boolean shouldExecuteToday(PersonaStatusEntity persona, LocalDate today) {
        if (persona.getFrequency() == null || persona.getColumnName() == null) {
            return false;
        }
        int days = getDaysFromFrequency(persona.getFrequency());

        LocalDate lastExecutionDate = persona.getLastUpdatedTimestamp() != null
                ? Util.convertToLocalDate(persona.getLastUpdatedTimestamp())
                : Util.convertToLocalDate(persona.getCreationTimestamp());

        return lastExecutionDate.plusDays(days).isEqual(today);
    }

    private int getDaysFromFrequency(Frequency frequency) {
        return switch (frequency) {
            case NONE -> 10000;
            case WEEKLY -> 7;
            case FIFTEEN_DAYS -> 15;
            case THIRTY_DAYS -> 30;
            case NINETY_DAYS -> 90;
        };
    }

    public static List<String> extractWebsitesList(Map<Object, Object> jsonMap) {
        List<String> websites = new ArrayList<>();

        // Get the results list
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) jsonMap.get("results");

        // Process each result
        for (Map<String, Object> result : results) {
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) result.get("properties");

            if (properties != null && properties.containsKey("website")) {
                String website = (String) properties.get("website");
                websites.add(website);
            }
        }

        return websites;
    }

//    public OverlapRecordEntity saveOverlapRecords(OverlapRecordsRequest request) {
//        log.info("overlap record start: ", request);
//        // Check if record already exists
//        OverlapRecordEntity existingRecord = overlapRecordsRepository.findByOrganizationIdAndRecordType(
//                request.getOrganizationId(),
//                request.getRecordType()
//        ).orElse(null);
//
//        OverlapRecordEntity overlapRecordEntity;
//        if (existingRecord != null) {
//            // Update existing record
//            overlapRecordEntity = existingRecord;
//            overlapRecordEntity.setVersion(existingRecord.getVersion() + 1);
//            log.info("Updating existing overlap record for organizationId: {}, recordType: {}, fileName: {}",
//                    request.getOrganizationId(), request.getRecordType(), request.getFileName());
//        } else {
//            // Create new record
//            overlapRecordEntity = new OverlapRecordEntity();
//            overlapRecordEntity.setVersion(1);
//            log.info("Creating new overlap record, version: 1");
//            log.info("Creating new overlap record for organizationId: {}, recordType: {}, fileName: {}",
//                    request.getOrganizationId(), request.getRecordType(), request.getFileName());
//        }
//
//        // Update or set fields
//        overlapRecordEntity.setOrganizationId(request.getOrganizationId());
//        overlapRecordEntity.setRecordType(request.getRecordType());
//        overlapRecordEntity.setFileName(request.getFileName());
//        overlapRecordEntity.setSource(request.getSource());
//        overlapRecordEntity.setFrequency(request.getFrequency());
//        overlapRecordEntity.setGoogleSheetLink(request.getGoogleSheetLink());
//        overlapRecordEntity.setFieldToColumnMapping(request.getFieldToColumnMapping());
//
//        // Process fields
//        List<String> websites = new ArrayList<>();
//        if (request.getFields() != null && !request.getFields().isEmpty()) {
//            // Clear existing fields if updating
//            if (existingRecord != null && existingRecord.getFields() != null) {
//                existingRecord.getFields().clear();
//            }
//
//            List<OverlapRecordFieldEntity> fieldEntities = request.getFields().stream().map(fieldRequest -> {
//                OverlapRecordFieldEntity fieldEntity = new OverlapRecordFieldEntity();
//                fieldEntity.setName(fieldRequest.getName());
//                fieldEntity.setCompanyName(fieldRequest.getCompanyName());
//                fieldEntity.setContactEmail(fieldRequest.getContactEmail());
//                fieldEntity.setDomain(fieldRequest.getDomain());
//                fieldEntity.setDealStage(fieldRequest.getDealStage());
//                fieldEntity.setCreationDate(fieldRequest.getCreationDate());
//                fieldEntity.setCloseDate(fieldRequest.getCloseDate());
//                fieldEntity.setSubscribed(fieldRequest.getSubscribed());
//                fieldEntity.setTicketSize(fieldRequest.getTicketSize());
//                fieldEntity.setOverlapRecord(overlapRecordEntity);
//                websites.add(fieldRequest.getDomain());
//                return fieldEntity;
//            }).toList();
//
//            // clear & add instead of replacing
//            overlapRecordEntity.getFields().clear();
//            overlapRecordEntity.getFields().addAll(fieldEntities);
//        }
//
//        // Update or create persona status
//        var freq = Frequency.valueOf(request.getFrequency().name());
//        PersonaRequest personaRequest = new PersonaRequest(
//                request.getOrganizationId(),
//                websites,
//                new String[]{},
//                freq,
//                "",
//                request.getFieldToColumnMapping().get("domain"),
//                request.getGoogleSheetLink(),
//                request.getSource(),
//                request.getFileName()
//        );
//
//        PersonaStatusEntity status = personaStatusRepository.getByOrganizationId(request.getOrganizationId());
//        if (status != null) {
//            // Update existing status
//            status.setColumnName(personaRequest.getColumnName());
//            status.setGoogleSheetLink(personaRequest.getGoogleSheetLink());
//            status.setPersonaMode(personaRequest.getPersonaMode());
//            status.setFileName(personaRequest.getFileName());
//            status.setFrequency(personaRequest.getFrequency());
////            status.setPersonaStatus(PersonaStatus.INITIATED);
//
//            log.info("Updating existing persona status for organizationId: {}", request.getOrganizationId());
//        } else {
//            log.info("request.getOrganizationId(): {}", request.getOrganizationId());
//            log.info("request.getRecordType(): {}", request.getRecordType());
//
//            // Create new status
//            status = new PersonaStatusEntity();
//            status.setColumnName(personaRequest.getColumnName());
//            status.setGoogleSheetLink(personaRequest.getGoogleSheetLink());
//            status.setPersonaMode(personaRequest.getPersonaMode());
//            status.setFileName(personaRequest.getFileName());
//            status.setFrequency(personaRequest.getFrequency());
//            status.setPersonaStatus(PersonaStatus.INITIATED);
//            status.setOrganizationId(personaRequest.getOrganizationId());
//            log.info("Creating new persona status for organizationId: {}", request.getOrganizationId());
//        }
//
//        if(RecordType.CUSTOMER.equals(request.getRecordType())) {
//            personaStatusRepository.save(status);
//
//            // Process persona asynchronously
////            asyncPersonaService.savePersona(personaRequest);
//        }
//
//        OverlapRecordEntity savedRecord = overlapRecordsRepository.save(overlapRecordEntity);
//
//        // Sync to dynamic table
//        dynamicTableService.syncOverlapRecordToDynamicTable(savedRecord);
//
//        return savedRecord;
//    }



    public Map<String, Integer> getOverlapRecords(Long orgBId) {
        Long orgAId = Util.getOrgIdFromToken();
        // Step 1: Fetch all records by org and type
        Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords = fetchRecordsGroupedByType(orgAId);
        Map<RecordType, List<OverlapRecordFieldEntity>> orgBRecords = fetchRecordsGroupedByType(orgBId);

        // Step 2: Define matrix
        RecordType[] types = RecordType.values(); // CUSTOMER, PROSPECT, OPPORTUNITY
        Map<String, Integer> matrix = new HashMap<>();

        for (RecordType aType : types) {
            for (RecordType bType : types) {
                List<OverlapRecordFieldEntity> aList = orgARecords.getOrDefault(aType, List.of());
                List<OverlapRecordFieldEntity> bList = orgBRecords.getOrDefault(bType, List.of());

                int overlap = countOverlap(aList, bList);
                matrix.put(aType.name() + "_" + bType.name(), overlap);
            }
        }

        return matrix;
    }

    public Map<String, Integer> getOverlapRecordsForEPS(String userId) {
        Long orgAId = Util.getOrgIdFromToken();
        // Step 1: Fetch all records by org and type
        Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords = fetchRecordsGroupedByType(orgAId);
        Map<RecordType, List<ExternalPartnerOverlapRecordFieldEntity>> orgBRecords = fetchRecordsGroupedByTypeForExternalPartner(userId);

        // Step 2: Define matrix
        RecordType[] types = RecordType.values(); // CUSTOMER, PROSPECT, OPPORTUNITY
        Map<String, Integer> matrix = new HashMap<>();

        for (RecordType aType : types) {
            for (RecordType bType : types) {
                List<OverlapRecordFieldEntity> aList = orgARecords.getOrDefault(aType, List.of());
                List<ExternalPartnerOverlapRecordFieldEntity> bList = orgBRecords.getOrDefault(bType, List.of());

                int overlap = countOverlapForEPSandInternal(aList, bList);
                matrix.put(aType.name() + "_" + bType.name(), overlap);
            }
        }

        return matrix;
    }


    private Map<RecordType, List<OverlapRecordFieldEntity>> fetchRecordsGroupedByType(Long orgId) {
        List<OverlapRecordEntity> records = overlapRecordsRepository.findByOrganizationId(orgId);

        Map<RecordType, List<OverlapRecordFieldEntity>> grouped = new EnumMap<>(RecordType.class);
        for (OverlapRecordEntity overlapRecord : records) {
            grouped.computeIfAbsent(overlapRecord.getRecordType(), k -> new ArrayList<>()).addAll(overlapRecord.getFields());
        }
        return grouped;
    }

    private Map<RecordType, List<ExternalPartnerOverlapRecordFieldEntity>> fetchRecordsGroupedByTypeForExternalPartner(String userId) {
        List<ExternalPartnerOverlapRecordEntity> records = externalPartnerOverlapRecordsRepository.findByUserId(userId);

        Map<RecordType, List<ExternalPartnerOverlapRecordFieldEntity>> grouped = new EnumMap<>(RecordType.class);
        for (ExternalPartnerOverlapRecordEntity overlapRecord : records) {
            grouped.computeIfAbsent(overlapRecord.getRecordType(), k -> new ArrayList<>()).addAll(overlapRecord.getFields());
        }
        return grouped;
    }


    private int countOverlapForEPSandInternal(List<OverlapRecordFieldEntity> listA, List<ExternalPartnerOverlapRecordFieldEntity> listB) {
        Set<String> emailsA = listA.stream()
                .map(OverlapRecordFieldEntity::getContactEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> domainsA = listA.stream()
                .map(OverlapRecordFieldEntity::getDomain)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return (int) listB.stream()
                .filter(b -> (b.getContactEmail() != null && emailsA.contains(b.getContactEmail().toLowerCase())) ||
                        (b.getDomain() != null && domainsA.contains(b.getDomain().toLowerCase())))
                .count();
    }

    private int countOverlap(List<OverlapRecordFieldEntity> listA, List<OverlapRecordFieldEntity> listB) {
        Set<String> emailsA = listA.stream()
                .map(OverlapRecordFieldEntity::getContactEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> domainsA = listA.stream()
                .map(OverlapRecordFieldEntity::getDomain)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return (int) listB.stream()
                .filter(b -> (b.getContactEmail() != null && emailsA.contains(b.getContactEmail().toLowerCase())) ||
                        (b.getDomain() != null && domainsA.contains(b.getDomain().toLowerCase())))
                .count();
    }

    public PartnerDataPermissionEntity setPartnerPermissions(PartnerDataPermission request) {
        // Validate the request
        if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
            throw new SharkdomException(ErrorMessages.SH92);
        }

        // Process each record type permission
        for (Map.Entry<RecordType, PartnerDataPermission.PermissionDetails> entry : request.getPermissions().entrySet()) {
            PartnerDataPermission.PermissionDetails details = entry.getValue();

            if (details.getAccessType() == PartnerDataPermission.AccessType.PARTIAL) {
                if (details.getSharedFields() == null || details.getSharedFields().isEmpty()) {
                    throw new SharkdomException(ErrorMessages.SH93, entry.getKey());
                }
                details.getSharedFields().forEach(field -> {
                    try {
                        PartnerDataPermission.AvailableField.valueOf(field);
                    } catch (IllegalArgumentException e) {
                        throw new SharkdomException(ErrorMessages.SH94, field);
                    }
                });
            }
        }

        // Create or update the main permission entity
        PartnerDataPermissionEntity entity = partnerDataPermissionRepository
                .findByOrganizationIdAndCollaborationCategory(request.getOrganizationId(), request.getCollaborationCategory())
                .orElse(PartnerDataPermissionEntity.builder()
                        .organizationId(request.getOrganizationId())
                        .collaborationCategory(request.getCollaborationCategory())
                        .permissions(new HashMap<>())
                        .build());

        // Update existing permissions instead of clearing them
        if (entity.getPermissions() == null) {
            entity.setPermissions(new HashMap<>());
        }

        // Add or update permissions
        request.getPermissions().forEach((recordType, modelDetails) -> {
            PartnerDataPermissionDetails details = entity.getPermissions().get(recordType);
            if (details == null) {
                details = PartnerDataPermissionDetails.builder()
                        .partnerDataPermission(entity)
                        .recordType(recordType)
                        .build();
            }
            details.setAccessType(modelDetails.getAccessType());
            details.setSharedFields(modelDetails.getSharedFields());
            entity.getPermissions().put(recordType, details);
        });

        return partnerDataPermissionRepository.save(entity);
    }

    public Map<String, Object> getPartnerDataWithPermissions(Long partnerId, String typeCombination) {
        Long organizationId = Util.getOrgIdFromToken();
        // First get collaboration categories for both organizations
        CollaborationCategory orgCategory = organizationCollaborationService.getCollaborationCategory(partnerId);

        // Get permissions for the organization
        PartnerDataPermissionEntity permissions = partnerDataPermissionRepository
                .findByOrganizationIdAndCollaborationCategory(organizationId, orgCategory)
                .orElse(PartnerDataPermissionEntity.builder()
                        .organizationId(organizationId)
                        .collaborationCategory(orgCategory)
                        .permissions(new HashMap<>())
                        .build());

        // Add default permissions if none exist
        if (permissions.getPermissions() == null || permissions.getPermissions().isEmpty()) {
            Map<RecordType, PartnerDataPermissionDetails> defaultPermissions = new HashMap<>();
            for (RecordType recordType : RecordType.values()) {
                defaultPermissions.put(recordType, PartnerDataPermissionDetails.builder()
                        .recordType(recordType)
                        .accessType(PartnerDataPermission.AccessType.FULL_ACCESS)
                        .sharedFields(Set.of())
                        .build());
            }
            permissions.setPermissions(defaultPermissions);
        }

        Map<String, Object> response = new HashMap<>();

        // If specific combination is requested
        if (typeCombination != null && !typeCombination.isEmpty()) {
            String[] types = typeCombination.split("_");
            if (types.length != 2) {
                throw new SharkdomException(ErrorMessages.SH95);
            }

            RecordType typeA = RecordType.valueOf(types[0]);
            RecordType typeB = RecordType.valueOf(types[1]);

            // Get the permission details for the requested record type
            PartnerDataPermissionDetails permissionDetails = permissions.getPermissions().get(typeA);
            if (permissionDetails == null) {
                throw new SharkdomException(ErrorMessages.SH96, typeA);
            }

            switch (permissionDetails.getAccessType()) {
                case HIDDEN:
                    response.put(PartnerDataFields.MESSAGE, "Data access is hidden");
                    break;

                case ONLY_COUNT:
                    Map<String, Integer> counts = getOverlapRecords(partnerId);
                    response.put(PartnerDataFields.COUNTS, counts);
                    break;

                case PARTIAL:
                case FULL_ACCESS:
                    // Get records grouped by type for both organizations
                    Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords = fetchRecordsGroupedByType(organizationId);
                    Map<RecordType, List<OverlapRecordFieldEntity>> orgBRecords = fetchRecordsGroupedByType(partnerId);

                    Map<String, Object> data = createCombinationData(typeA, typeB, orgARecords, orgBRecords,
                            permissionDetails, getOverlapRecords(partnerId));
                    response.put(PartnerDataFields.DATA, data);
                    break;
            }
        } else {
            // Return full matrix
            Map<String, Object> matrix = new HashMap<>();
            Map<String, Integer> overlapCounts = getOverlapRecords(partnerId);

            // Organization A's sections
            Map<String, Object> customersA = new HashMap<>();
            Map<String, Object> prospectsA = new HashMap<>();
            Map<String, Object> opportunitiesA = new HashMap<>();

            // Get records grouped by type for both organizations
            Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords = fetchRecordsGroupedByType(organizationId);
            Map<RecordType, List<OverlapRecordFieldEntity>> orgBRecords = fetchRecordsGroupedByType(partnerId);

            // Process each combination for Organization A's Customers
            PartnerDataPermissionDetails customerPermissions = permissions.getPermissions().get(RecordType.CUSTOMER);
            if (customerPermissions != null && customerPermissions.getAccessType() != PartnerDataPermission.AccessType.HIDDEN) {
                customersA.put(PartnerDataFields.B_CUSTOMERS, createCombinationData(RecordType.CUSTOMER, RecordType.CUSTOMER, orgARecords, orgBRecords, customerPermissions, overlapCounts));
                customersA.put(PartnerDataFields.B_PROSPECTS, createCombinationData(RecordType.CUSTOMER, RecordType.PROSPECT, orgARecords, orgBRecords, customerPermissions, overlapCounts));
                customersA.put(PartnerDataFields.B_OPPORTUNITIES, createCombinationData(RecordType.CUSTOMER, RecordType.OPPORTUNITY, orgARecords, orgBRecords, customerPermissions, overlapCounts));
            }

            // Process each combination for Organization A's Prospects
            PartnerDataPermissionDetails prospectPermissions = permissions.getPermissions().get(RecordType.PROSPECT);
            if (prospectPermissions != null && prospectPermissions.getAccessType() != PartnerDataPermission.AccessType.HIDDEN) {
                prospectsA.put(PartnerDataFields.B_CUSTOMERS, createCombinationData(RecordType.PROSPECT, RecordType.CUSTOMER, orgARecords, orgBRecords, prospectPermissions, overlapCounts));
                prospectsA.put(PartnerDataFields.B_PROSPECTS, createCombinationData(RecordType.PROSPECT, RecordType.PROSPECT, orgARecords, orgBRecords, prospectPermissions, overlapCounts));
                prospectsA.put(PartnerDataFields.B_OPPORTUNITIES, createCombinationData(RecordType.PROSPECT, RecordType.OPPORTUNITY, orgARecords, orgBRecords, prospectPermissions, overlapCounts));
            }

            // Process each combination for Organization A's Opportunities
            PartnerDataPermissionDetails opportunityPermissions = permissions.getPermissions().get(RecordType.OPPORTUNITY);
            if (opportunityPermissions != null && opportunityPermissions.getAccessType() != PartnerDataPermission.AccessType.HIDDEN) {
                opportunitiesA.put(PartnerDataFields.B_CUSTOMERS, createCombinationData(RecordType.OPPORTUNITY, RecordType.CUSTOMER, orgARecords, orgBRecords, opportunityPermissions, overlapCounts));
                opportunitiesA.put(PartnerDataFields.B_PROSPECTS, createCombinationData(RecordType.OPPORTUNITY, RecordType.PROSPECT, orgARecords, orgBRecords, opportunityPermissions, overlapCounts));
                opportunitiesA.put(PartnerDataFields.B_OPPORTUNITIES, createCombinationData(RecordType.OPPORTUNITY, RecordType.OPPORTUNITY, orgARecords, orgBRecords, opportunityPermissions, overlapCounts));
            }

            matrix.put(PartnerDataFields.A_CUSTOMERS, customersA);
            matrix.put(PartnerDataFields.A_PROSPECTS, prospectsA);
            matrix.put(PartnerDataFields.A_OPPORTUNITIES, opportunitiesA);

            response.put(PartnerDataFields.MATRIX, matrix);
        }
        response.put("organizationName", organizationRepository.findNameById(organizationId));
        response.put("partnerName", organizationRepository.findNameById(partnerId));
        return response;
    }

    public List<OverlapRecordFieldEntity> getOverlappingRecords(List<OverlapRecordFieldEntity> listA, List<OverlapRecordFieldEntity> listB) {
        Set<String> emailsA = listA.stream()
                .map(OverlapRecordFieldEntity::getContactEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> domainsA = listA.stream()
                .map(OverlapRecordFieldEntity::getDomain)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return listB.stream()
                .filter(b -> (b.getContactEmail() != null && emailsA.contains(b.getContactEmail().toLowerCase())) ||
                        (b.getDomain() != null && domainsA.contains(b.getDomain().toLowerCase())))
                .collect(Collectors.toList());
    }

    private Map<String, Object> createCombinationData(RecordType typeA, RecordType typeB,
                                                      Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords,
                                                      Map<RecordType, List<OverlapRecordFieldEntity>> orgBRecords,
                                                      PartnerDataPermissionDetails permissionDetails,
                                                      Map<String, Integer> overlapCounts) {
        List<OverlapRecordFieldEntity> aList = orgARecords.getOrDefault(typeA, List.of());
        List<OverlapRecordFieldEntity> bList = orgBRecords.getOrDefault(typeB, List.of());

        // Get overlapping records from B that match with A
        List<OverlapRecordFieldEntity> overlappingBRecords = getOverlappingRecords(aList, bList);

        // Get overlapping records from A that match with B
        List<OverlapRecordFieldEntity> overlappingARecords = getOverlappingRecords(bList, aList);

        // Apply field filtering based on permissions
        if (permissionDetails.getAccessType() == PartnerDataPermission.AccessType.PARTIAL) {
            overlappingARecords = filterFields(overlappingARecords, permissionDetails.getSharedFields());
            overlappingBRecords = filterFields(overlappingBRecords, permissionDetails.getSharedFields());
        }

        // Combine the records into a single list
        List<Map<String, Object>> combinedRecords = new ArrayList<>();

        // Create a map to store records by their key (email or domain)
        Map<String, Map<String, Object>> recordMap = new HashMap<>();

        // Process organization records
        for (OverlapRecordFieldEntity orgRecord : overlappingARecords) {
            String key = orgRecord.getContactEmail() != null ? orgRecord.getContactEmail().toLowerCase() :
                    orgRecord.getDomain() != null ? orgRecord.getDomain().toLowerCase() : null;
            if (key != null) {
                Map<String, Object> record = new HashMap<>();
                record.put("organization_record", orgRecord);
                recordMap.put(key, record);
            }
        }

        // Process partner records and combine with organization records
        for (OverlapRecordFieldEntity partnerRecord : overlappingBRecords) {
            String key = partnerRecord.getContactEmail() != null ? partnerRecord.getContactEmail().toLowerCase() :
                    partnerRecord.getDomain() != null ? partnerRecord.getDomain().toLowerCase() : null;
            if (key != null) {
                Map<String, Object> record = recordMap.getOrDefault(key, new HashMap<>());
                record.put("partner_record", partnerRecord);
                if (!recordMap.containsKey(key)) {
                    recordMap.put(key, record);
                }
            }
        }

        // Convert the map to a list
        combinedRecords.addAll(recordMap.values());

        Map<String, Object> combinationData = new HashMap<>();
        combinationData.put(PartnerDataFields.RECORDS, combinedRecords);
        combinationData.put(PartnerDataFields.OVERLAP_COUNT, overlapCounts.getOrDefault(typeA.name() + "_" + typeB.name(), 0));

        combinationData.put("raw_records_A", aList.size());
        combinationData.put("raw_records_B", bList.size());
        combinationData.put("raw_records_sum",aList.size()+ bList.size());
        combinationData.put("overlap_customer_count", combinedRecords.size());
        return combinationData;
    }

    private List<OverlapRecordFieldEntity> filterFields(List<OverlapRecordFieldEntity> records, Set<String> sharedFields) {
        return records.stream().map(field -> {
            OverlapRecordFieldEntity filtered = new OverlapRecordFieldEntity();
            filtered.setName(sharedFields.contains(PartnerDataFields.NAME) ? field.getName() : NOT_SHARED);
            filtered.setCompanyName(sharedFields.contains(PartnerDataFields.COMPANY_NAME) ? field.getCompanyName() : NOT_SHARED);
            filtered.setContactEmail(sharedFields.contains(PartnerDataFields.CONTACT_EMAIL) ? field.getContactEmail() : NOT_SHARED);
            filtered.setDomain(sharedFields.contains(PartnerDataFields.DOMAIN) ? field.getDomain() : NOT_SHARED);
            filtered.setDealStage(sharedFields.contains(PartnerDataFields.DEAL_STAGE) ? field.getDealStage() : NOT_SHARED);
            filtered.setCreationDate(sharedFields.contains(PartnerDataFields.CREATION_DATE) ? field.getCreationDate() : NOT_SHARED);
            filtered.setCloseDate(sharedFields.contains(PartnerDataFields.CLOSE_DATE) ? field.getCloseDate() : NOT_SHARED);
            filtered.setSubscribed(sharedFields.contains(PartnerDataFields.SUBSCRIBED) ? field.getSubscribed() : NOT_SHARED);
            filtered.setTicketSize(sharedFields.contains(PartnerDataFields.TICKET_SIZE) ? field.getTicketSize() : NOT_SHARED);
            return filtered;
        }).toList();
    }

    public PartnerDataPermissionResponse  getPartnerDataPermissions(Long organizationId) {
        // Get all permissions for the organization
        List<PartnerDataPermissionEntity> permissions = partnerDataPermissionRepository.findByOrganizationId(organizationId);

        // Get all collaboration categories
        Set<CollaborationCategory> allCategories = new HashSet<>(Arrays.asList(CollaborationCategory.values()));

        // Get existing categories from permissions
        Set<CollaborationCategory> existingCategories = permissions.stream()
                .map(PartnerDataPermissionEntity::getCollaborationCategory)
                .collect(Collectors.toSet());

        // Find missing categories
        Set<CollaborationCategory> missingCategories = new HashSet<>(allCategories);
        missingCategories.removeAll(existingCategories);

        // Convert existing permissions to response model
        List<PartnerDataPermissionResponse.PermissionDetails> permissionDetails = permissions.stream()
                .map(entity -> {
                    Map<RecordType, PartnerDataPermissionResponse.RecordPermission> recordPermissions = new HashMap<>();

                    entity.getPermissions().forEach((recordType, details) -> {
                        PartnerDataPermissionResponse.RecordPermission permission = new PartnerDataPermissionResponse.RecordPermission();
                        permission.setAccessType(details.getAccessType());
                        if (details.getAccessType() == PartnerDataPermission.AccessType.PARTIAL) {
                            permission.setSharedFields(details.getSharedFields());
                        }
                        recordPermissions.put(recordType, permission);
                    });

                    return PartnerDataPermissionResponse.PermissionDetails.builder()
                            .collaborationCategory(entity.getCollaborationCategory())
                            .recordPermissions(recordPermissions)
                            .lastModified(entity.getLastUpdatedTimestamp())
                            .build();
                })
                .collect(Collectors.toList());

        // Add full access for missing categories
        missingCategories.forEach(category -> {
            Map<RecordType, PartnerDataPermissionResponse.RecordPermission> fullAccessPermissions = Arrays.stream(RecordType.values())
                    .collect(Collectors.toMap(
                            type -> type,
                            type -> {
                                PartnerDataPermissionResponse.RecordPermission permission = new PartnerDataPermissionResponse.RecordPermission();
                                permission.setAccessType(PartnerDataPermission.AccessType.FULL_ACCESS);
                                return permission;
                            }
                    ));

            permissionDetails.add(PartnerDataPermissionResponse.PermissionDetails.builder()
                    .collaborationCategory(category)
                    .recordPermissions(fullAccessPermissions)
                    .lastModified(new Date())
                    .build());
        });

        return PartnerDataPermissionResponse.builder()
                .organizationId(organizationId)
                .permissions(permissionDetails)
                .build();
    }

    public List<OverlapRecordEntity> getOverlapRecords(RecordType recordType) {
        Long organizationId = Util.getOrgIdFromToken();
        if (recordType != null) {
            var result = overlapRecordsRepository.findByOrganizationIdAndRecordType(organizationId, recordType);
            return result.map(List::of).orElseGet(ArrayList::new);
        } else {
            return overlapRecordsRepository.findByOrganizationId(organizationId);
        }
    }

    public List<OverlapRecordEntity> getOverlapRecord(RecordType recordType,Long organizationId) {
        if (recordType != null) {
            var result = overlapRecordsRepository.findByOrganizationIdAndRecordType(organizationId, recordType);
            return result.map(List::of).orElseGet(ArrayList::new);
        } else {
            return overlapRecordsRepository.findByOrganizationId(organizationId);
        }
    }

    public List<ExternalPartnerOverlapRecordEntity> getOverlapRecordsForUser(String userId,RecordType recordType) {
        if (recordType != null) {
            var result = externalPartnerOverlapRecordsRepository.findByUserIdAndRecordType(userId, recordType);
            return result.map(List::of).orElseGet(ArrayList::new);
        } else {
            return externalPartnerOverlapRecordsRepository.findByUserId(userId);
        }
    }

    @Transactional
    public void deleteOverlapRecords(RecordType recordType) {
        Long organizationId = Util.getOrgIdFromToken();
        log.info("Starting deletion of records for organizationId: {} and recordType: {}", organizationId, recordType);

        // Delete overlap records
        if (recordType != null) {
            Optional<OverlapRecordEntity> records = overlapRecordsRepository.findByOrganizationIdAndRecordType(organizationId, recordType);
            records.ifPresent(overlapRecordsRepository::delete);
        } else {
            List<OverlapRecordEntity> records = overlapRecordsRepository.findByOrganizationId(organizationId);
            if (!records.isEmpty()) {
                log.info("Deleting {} overlap records", records.size());
                // Delete all fields first to avoid foreign key constraint issues
                records.forEach(record -> {
                    if (record.getFields() != null) {
                        record.getFields().clear();
                    }
                });
                overlapRecordsRepository.deleteAll(records);
            }
        }

        // Delete persona status
        try {
            PersonaStatusEntity status = personaStatusRepository.getByOrganizationId(organizationId);
            if (status != null) {
                log.info("Deleting persona status for organizationId: {}", organizationId);
                personaStatusRepository.delete(status);
            }
        } catch (Exception e) {
            log.error("Error deleting persona status for organizationId: {}", organizationId, e);
            throw new SharkdomException(ErrorMessages.SH97, e.getMessage());
        }

        // Delete persona details
        try {
            log.info("Deleting persona details for organizationId: {}", organizationId);
            personaDetailsRepository.deleteByOrganizationId(organizationId);
        } catch (Exception e) {
            log.error("Error deleting persona details for organizationId: {}", organizationId, e);
            throw new SharkdomException(ErrorMessages.SH98, e.getMessage());
        }

        // Delete persona records
        try {
            List<PersonaEntity> personaRecords = personaRepository.getAllByOrganizationId(organizationId);
            if (!personaRecords.isEmpty()) {
                log.info("Deleting {} persona records", personaRecords.size());
                personaRepository.deleteAll(personaRecords);
            }
        } catch (Exception e) {
            log.error("Error deleting persona records for organizationId: {}", organizationId, e);
            throw new SharkdomException(ErrorMessages.SH100, e.getMessage());
        }

        //Delete persona user notify details
        try {
            List<PersonaUserNotifyEntity> personaUserNotifyEntities = personaUserNotifyRepository.findAllByRecieverOrgId(organizationId);
            if (!personaUserNotifyEntities.isEmpty()) {
                log.info("Deleting {} persona user notify records", personaUserNotifyEntities.size());
                personaUserNotifyRepository.deleteAll(personaUserNotifyEntities);
            }
        } catch (Exception e) {
            log.error("Error deleting persona user notify records");
        }

        // Delete partner data permissions
        try {
            List<PartnerDataPermissionEntity> permissions = partnerDataPermissionRepository.findByOrganizationId(organizationId);
            if (!permissions.isEmpty()) {
                log.info("Deleting {} partner data permissions", permissions.size());
                permissions.forEach(permission -> {
                    if (permission.getPermissions() != null) {
                        permission.getPermissions().clear();
                    }
                });
                partnerDataPermissionRepository.deleteAll(permissions);
            }
        } catch (Exception e) {
            log.error("Error deleting partner data permissions for organizationId: {}", organizationId, e);
            throw new SharkdomException(ErrorMessages.SH101, e.getMessage());
        }

        log.info("Successfully completed deletion of all records for organizationId: {}", organizationId);
    }

    public PersonaMatchDto getPersonaMatch(Long organizationId) {
        PersonaStatusEntity currentPersona = personaStatusRepository.getByOrganizationId(Util.getOrgIdFromToken());
        PersonaStatusEntity personaToBeMatched = personaStatusRepository.getByOrganizationId(organizationId);
        Boolean currentOrgPersonaStatus = Objects.nonNull(currentPersona) && currentPersona.getPersonaStatus().equals(PersonaStatus.COMPLETED);
        Boolean targetOrgPersonaStatus = Objects.nonNull(personaToBeMatched) && personaToBeMatched.getPersonaStatus().equals(PersonaStatus.COMPLETED);
        return PersonaMatchDto.builder().currentOrgPersonaStatus(currentOrgPersonaStatus).targetOrgPersonaStatus(targetOrgPersonaStatus).personaMatch(calculateOverlappingPercentage(Util.getOrgIdFromToken(), organizationId)).build();
    }

    private Map<String, Object> calculateOverlappingPercentage(Long orgIdFromToken, Long organizationId) {
        Map<String, Object> percentageCalculations = new HashMap<>();
        Map<String, List<PercentageCategory>> currentPersona = getAllData(orgIdFromToken);
        Map<String, List<PercentageCategory>> targetPersona = getAllData(organizationId);
        percentageCalculations.put("compatibilityMatch", CategoryComparator.calculatePercentageMatch(currentPersona.get("marketSegment"), targetPersona.get("marketSegment")));
        percentageCalculations.put("audienceOverlap", CategoryComparator.calculatePercentageMatch(currentPersona.get("companySector"), targetPersona.get("companySector")));
        percentageCalculations.put("audienceMatch", CategoryComparator.CompanySizeRank.fromLabel(CategoryComparator.getLargestMatchingCompanySize(currentPersona.get("companySize"), targetPersona.get("companySize"))));
        percentageCalculations.put("breakDownCompatibilityMatch", CategoryComparator.calculateBreakdownFromLists(currentPersona.get("marketSegment"), targetPersona.get("marketSegment")));
        percentageCalculations.put("breakDownAudienceOverlap", CategoryComparator.calculateBreakdownFromLists(currentPersona.get("companySector"), targetPersona.get("companySector")));
        return percentageCalculations;
    }

    public Boolean saveOrUpdateNotify(Long senderOrgId, Long receiverOrgId) {
        PersonaStatusEntity personaStatusEntity = personaStatusRepository.getByOrganizationId(receiverOrgId);
        if (!organizationRepository.existsOrganizationById(receiverOrgId) || senderOrgId.equals(receiverOrgId)) {
            return false;
        }
        boolean isQualified = false;
        isQualified = Objects.isNull(personaStatusEntity);
        if (!isQualified) {
            isQualified = personaStatusEntity.getPersonaStatus().equals(PersonaStatus.COMPLETED);
        }
        if (isQualified) {
            PersonaUserNotifyEntity entity = personaUserNotifyRepository
                    .findBySenderOrgIdAndRecieverOrgId(senderOrgId, receiverOrgId)
                    .map(existing -> {
                        existing.setIsNotified(false);
                        return existing;
                    })
                    .orElseGet(() -> {
                        PersonaUserNotifyEntity newEntity = new PersonaUserNotifyEntity();
                        newEntity.setSenderOrgId(senderOrgId);
                        newEntity.setRecieverOrgId(receiverOrgId);
                        newEntity.setIsNotified(false);
                        return newEntity;
                    });
            personaUserNotifyRepository.save(entity);
        }
        return isQualified;
    }

    public void removeLatestPersonaAndIntegration(Long orgIdFromToken) {
        log.info("Pausing Persona Integration"+ orgIdFromToken);
        PersonaStatusEntity personaStatusEntity = personaStatusRepository.getByOrganizationId(orgIdFromToken);
        if (personaStatusEntity != null) {
            personaStatusEntity.setPersonaStatus(PersonaStatus.PAUSED);
            personaStatusRepository.save(personaStatusEntity);
            Map<String, IntegrationType> integrationTypeMap = new HashMap<>();
            integrationTypeMap.put("HUBSPOT", IntegrationType.HUBSPOT);
            integrationTypeMap.put("GOOGLE_SHEET", IntegrationType.G_SHEET);
            integrationTypeMap.put("ZOHO", IntegrationType.ZOHO);
            integrationRepository.deleteByOrganizationIdAndIntegrationType(orgIdFromToken, integrationTypeMap.get(personaStatusEntity.getPersonaMode().name()));
        }
    }

    @Transactional
    public void deleteOverlapRecord(RecordType recordType) {
        Long organizationId = Util.getOrgIdFromToken();
        log.info("Starting deletion of records for organizationId: {} and recordType: {}", organizationId, recordType);

        if (recordType == RecordType.OPPORTUNITY || recordType == RecordType.PROSPECT) {
            overlapRecordsRepository.findByOrganizationIdAndRecordType(organizationId, recordType)
                    .ifPresent(overlapRecordsRepository::delete);
        }

        log.info("Successfully completed deletion of records for organizationId: {} and recordType: {}", organizationId, recordType);
    }


    public ExternalPartnerOverlapRecordEntity saveOverlapRecordsForUser(OverlapRecordsRequest request) {
        // Check if record already exists
        ExternalPartnerOverlapRecordEntity existingRecord = externalPartnerOverlapRecordsRepository.findByUserIdAndRecordType(
                request.getUserId(),
                request.getRecordType()
        ).orElse(null);

        ExternalPartnerOverlapRecordEntity overlapRecordEntity;
        if (existingRecord != null) {
            // Update existing record
            overlapRecordEntity = existingRecord;
            log.info("Updating existing overlap record for organizationId: {}, recordType: {}, fileName: {}",
                    request.getUserId(), request.getRecordType(), request.getFileName());
        } else {
            // Create new record
            overlapRecordEntity = new ExternalPartnerOverlapRecordEntity();
            log.info("Creating new overlap record for organizationId: {}, recordType: {}, fileName: {}",
                    request.getUserId(), request.getRecordType(), request.getFileName());
        }

        // Update or set fields
        overlapRecordEntity.setUserId(request.getUserId());
        overlapRecordEntity.setRecordType(request.getRecordType());
        overlapRecordEntity.setFileName(request.getFileName());
        overlapRecordEntity.setSource(request.getSource());
        overlapRecordEntity.setFrequency(request.getFrequency());
        overlapRecordEntity.setGoogleSheetLink(request.getGoogleSheetLink());
        overlapRecordEntity.setFieldToColumnMapping(request.getFieldToColumnMapping());

        // Process fields
        List<String> websites = new ArrayList<>();
        if (request.getFields() != null && !request.getFields().isEmpty()) {
            // Clear existing fields if updating
            if (existingRecord != null && existingRecord.getFields() != null) {
                existingRecord.getFields().clear();
            }

            List<ExternalPartnerOverlapRecordFieldEntity> fieldEntities = request.getFields().stream().map(fieldRequest -> {
                ExternalPartnerOverlapRecordFieldEntity fieldEntity = new ExternalPartnerOverlapRecordFieldEntity();
                fieldEntity.setName(fieldRequest.getName());
                fieldEntity.setCompanyName(fieldRequest.getCompanyName());
                fieldEntity.setContactEmail(fieldRequest.getContactEmail());
                fieldEntity.setDomain(fieldRequest.getDomain());
                fieldEntity.setDealStage(fieldRequest.getDealStage());
                fieldEntity.setCreationDate(fieldRequest.getCreationDate());
                fieldEntity.setCloseDate(fieldRequest.getCloseDate());
                fieldEntity.setSubscribed(fieldRequest.getSubscribed());
                fieldEntity.setTicketSize(fieldRequest.getTicketSize());
                fieldEntity.setOverlapRecord(overlapRecordEntity);
                websites.add(fieldRequest.getDomain());
                return fieldEntity;
            }).toList();

            // clear & add instead of replacing
            overlapRecordEntity.getFields().clear();
            overlapRecordEntity.getFields().addAll(fieldEntities);
        }

        return externalPartnerOverlapRecordsRepository.save(overlapRecordEntity);
    }


    public List<ExternalPartnerOverlapRecordFieldEntity> getOverlappingRecordsForUser(List<ExternalPartnerOverlapRecordFieldEntity> listA, List<ExternalPartnerOverlapRecordFieldEntity> listB) {
        Set<String> emailsA = listA.stream()
                .map(ExternalPartnerOverlapRecordFieldEntity::getContactEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> domainsA = listA.stream()
                .map(ExternalPartnerOverlapRecordFieldEntity::getDomain)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return listB.stream()
                .filter(b -> (b.getContactEmail() != null && emailsA.contains(b.getContactEmail().toLowerCase())) ||
                        (b.getDomain() != null && domainsA.contains(b.getDomain().toLowerCase())))
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteOverlapRecordsForUser(RecordType recordType,String userId) {
        log.info("Starting deletion of records for organizationId: {} and recordType: {}", userId, recordType);

        // Delete overlap records
        if (recordType != null) {
            Optional<ExternalPartnerOverlapRecordEntity> records = externalPartnerOverlapRecordsRepository.findByUserIdAndRecordType(userId, recordType);
            records.ifPresent(externalPartnerOverlapRecordsRepository::delete);
        } else {
            List<ExternalPartnerOverlapRecordEntity> records = externalPartnerOverlapRecordsRepository.findByUserId(userId);
            if (!records.isEmpty()) {
                log.info("Deleting {} overlap records", records.size());
                // Delete all fields first to avoid foreign key constraint issues
                records.forEach(record -> {
                    if (record.getFields() != null) {
                        record.getFields().clear();
                    }
                });
                externalPartnerOverlapRecordsRepository.deleteAll(records);
            }
        }

        log.info("Successfully completed deletion of all records for userId: {}", userId);
    }

    public int countOverlapUser(List<ExternalPartnerOverlapRecordFieldEntity> listA, List<ExternalPartnerOverlapRecordFieldEntity> listB) {
        Set<String> emailsA = listA.stream()
                .map(ExternalPartnerOverlapRecordFieldEntity::getContactEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> domainsA = listA.stream()
                .map(ExternalPartnerOverlapRecordFieldEntity::getDomain)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return (int) listB.stream()
                .filter(b -> (b.getContactEmail() != null && emailsA.contains(b.getContactEmail().toLowerCase())) ||
                        (b.getDomain() != null && domainsA.contains(b.getDomain().toLowerCase())))
                .count();
    }

    public void removeLatestPersonaAndIntegrationFromCRM(IntegrationType integrationType) {
        log.info("Disconnecting integration for type: {}", integrationType);

        Long orgId = Util.getOrgIdFromToken();

        try {
            var orgIntegration = integrationRepository
                    .findByOrganizationIdAndIntegrationType(orgId, integrationType);

            // Case 1: Integration not found
            if (orgIntegration == null) {
                throw new ServiceException(
                        ErrorMessages.NOT_FOUND,
                        "Integration not found for organizationId: " + orgId +
                                " and integrationType: " + integrationType
                );
            }

            // Case 2: Already disconnected
            if (!Boolean.TRUE.equals(orgIntegration.isConnected())) {
                throw new ServiceException(
                        ErrorMessages.SH106,
                        "Integration already disconnected"
                );
            }

            // Disconnect integration
            orgIntegration.setConnected(false);
            orgIntegration.setRefreshToken(null);

            integrationRepository.save(orgIntegration);

            log.info("Integration disconnected successfully for orgId: {} and type: {}",
                    orgId, integrationType);

        } catch (ServiceException ex) {
            log.error("ServiceException while disconnecting integration: {}", ex.getMessage());
            throw ex;

        } catch (Exception ex) {
            log.error("Unexpected error while disconnecting integration", ex);

            throw new ServiceException(
                    ErrorMessages.SH146,
                    "disconnect integration",
                    ex.getMessage()
            );
        }
    }


    public Map<String, Object> getPartnerData(String userId, Long organizationId) {
        Map<String, Object> response = new HashMap<>();
        // Return full matrix
        Map<String, Object> matrix = new HashMap<>();
        Map<String, Integer> overlapCounts = getOverlapRecordsForEPS(userId);

        // Organization A's sections
        Map<String, Object> customersA = new HashMap<>();
        Map<String, Object> prospectsA = new HashMap<>();
        Map<String, Object> opportunitiesA = new HashMap<>();

        // Get records grouped by type for both organizations
        Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords = fetchRecordsGroupedByType(organizationId);
        Map<RecordType, List<ExternalPartnerOverlapRecordFieldEntity>> orgBRecords = fetchRecordsGroupedByTypeForExternalPartner(userId);


        customersA.put(PartnerDataFields.B_CUSTOMERS, createCombinationDataForEPS(RecordType.CUSTOMER, RecordType.CUSTOMER, orgARecords, orgBRecords, overlapCounts));
        customersA.put(PartnerDataFields.B_PROSPECTS, createCombinationDataForEPS(RecordType.CUSTOMER, RecordType.PROSPECT, orgARecords, orgBRecords, overlapCounts));
        customersA.put(PartnerDataFields.B_OPPORTUNITIES, createCombinationDataForEPS(RecordType.CUSTOMER, RecordType.OPPORTUNITY, orgARecords, orgBRecords, overlapCounts));


        // Process each combination for Organization A's Prospects

        prospectsA.put(PartnerDataFields.B_CUSTOMERS, createCombinationDataForEPS(RecordType.PROSPECT, RecordType.CUSTOMER, orgARecords, orgBRecords, overlapCounts));
        prospectsA.put(PartnerDataFields.B_PROSPECTS, createCombinationDataForEPS(RecordType.PROSPECT, RecordType.PROSPECT, orgARecords, orgBRecords, overlapCounts));
        prospectsA.put(PartnerDataFields.B_OPPORTUNITIES, createCombinationDataForEPS(RecordType.PROSPECT, RecordType.OPPORTUNITY, orgARecords, orgBRecords, overlapCounts));

        // Process each combination for Organization A's Opportunities

        opportunitiesA.put(PartnerDataFields.B_CUSTOMERS, createCombinationDataForEPS(RecordType.OPPORTUNITY, RecordType.CUSTOMER, orgARecords, orgBRecords, overlapCounts));
        opportunitiesA.put(PartnerDataFields.B_PROSPECTS, createCombinationDataForEPS(RecordType.OPPORTUNITY, RecordType.PROSPECT, orgARecords, orgBRecords, overlapCounts));
        opportunitiesA.put(PartnerDataFields.B_OPPORTUNITIES, createCombinationDataForEPS(RecordType.OPPORTUNITY, RecordType.OPPORTUNITY, orgARecords, orgBRecords, overlapCounts));


        matrix.put(PartnerDataFields.A_CUSTOMERS, customersA);
        matrix.put(PartnerDataFields.A_PROSPECTS, prospectsA);
        matrix.put(PartnerDataFields.A_OPPORTUNITIES, opportunitiesA);

        response.put(PartnerDataFields.MATRIX, matrix);

        response.put("organizationName", organizationRepository.findNameById(organizationId));
        return response;
    }

    private Map<String, Object> createCombinationDataForEPS(RecordType typeA, RecordType typeB,
                                                            Map<RecordType, List<OverlapRecordFieldEntity>> orgARecords,
                                                            Map<RecordType, List<ExternalPartnerOverlapRecordFieldEntity>> orgBRecords,
                                                            Map<String, Integer> overlapCounts) {
        List<OverlapRecordFieldEntity> aList = orgARecords.getOrDefault(typeA, List.of());
        List<ExternalPartnerOverlapRecordFieldEntity> bList = orgBRecords.getOrDefault(typeB, List.of());

        // Get overlapping records from B that match with A
        List<ExternalPartnerOverlapRecordFieldEntity> overlappingBRecords = getOverlappingRecordsForEPS(aList, bList);

        // Get overlapping records from A that match with B
        List<OverlapRecordFieldEntity> overlappingARecords = getOverlappingRecordsForEPSBTOA(bList, aList);


        // Combine the records into a single list
        List<Map<String, Object>> combinedRecords = new ArrayList<>();

        // Create a map to store records by their key (email or domain)
        Map<String, Map<String, Object>> recordMap = new HashMap<>();

        // Process organization records
        for (OverlapRecordFieldEntity orgRecord : overlappingARecords) {
            String key = orgRecord.getContactEmail() != null ? orgRecord.getContactEmail().toLowerCase() :
                    orgRecord.getDomain() != null ? orgRecord.getDomain().toLowerCase() : null;
            if (key != null) {
                Map<String, Object> record = new HashMap<>();
                record.put("organization_record", orgRecord);
                recordMap.put(key, record);
            }
        }

        // Process partner records and combine with organization records
        for (ExternalPartnerOverlapRecordFieldEntity partnerRecord : overlappingBRecords) {
            String key = partnerRecord.getContactEmail() != null ? partnerRecord.getContactEmail().toLowerCase() :
                    partnerRecord.getDomain() != null ? partnerRecord.getDomain().toLowerCase() : null;
            if (key != null) {
                Map<String, Object> record = recordMap.getOrDefault(key, new HashMap<>());
                record.put("partner_record", partnerRecord);
                if (!recordMap.containsKey(key)) {
                    recordMap.put(key, record);
                }
            }
        }

        // Convert the map to a list
        combinedRecords.addAll(recordMap.values());

        Map<String, Object> combinationData = new HashMap<>();
        combinationData.put(PartnerDataFields.RECORDS, combinedRecords);
        combinationData.put(PartnerDataFields.OVERLAP_COUNT, overlapCounts.getOrDefault(typeA.name() + "_" + typeB.name(), 0));

        combinationData.put("raw_records_A", aList.size());
        combinationData.put("raw_records_B", bList.size());
        combinationData.put("raw_records_sum", aList.size() + bList.size());
        combinationData.put("overlap_customer_count", combinedRecords.size());
        return combinationData;
    }

    public List<ExternalPartnerOverlapRecordFieldEntity> getOverlappingRecordsForEPS(List<OverlapRecordFieldEntity> listA, List<ExternalPartnerOverlapRecordFieldEntity> listB) {
        Set<String> emailsA = listA.stream()
                .map(OverlapRecordFieldEntity::getContactEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> domainsA = listA.stream()
                .map(OverlapRecordFieldEntity::getDomain)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return listB.stream()
                .filter(b -> (b.getContactEmail() != null && emailsA.contains(b.getContactEmail().toLowerCase())) ||
                        (b.getDomain() != null && domainsA.contains(b.getDomain().toLowerCase())))
                .collect(Collectors.toList());
    }

    public List<OverlapRecordFieldEntity> getOverlappingRecordsForEPSBTOA(List<ExternalPartnerOverlapRecordFieldEntity> listA, List<OverlapRecordFieldEntity> listB) {
        Set<String> emailsA = listA.stream()
                .map(ExternalPartnerOverlapRecordFieldEntity::getContactEmail)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> domainsA = listA.stream()
                .map(ExternalPartnerOverlapRecordFieldEntity::getDomain)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return listB.stream()
                .filter(b -> (b.getContactEmail() != null && emailsA.contains(b.getContactEmail().toLowerCase())) ||
                        (b.getDomain() != null && domainsA.contains(b.getDomain().toLowerCase())))
                .collect(Collectors.toList());
    }

    private Integer generateNextVersionId(Long orgId) {

        PersonaStatusEntity lastVersion =
                personaStatusRepository
                        .findTopByOrganizationIdOrderByVersionIdDesc(orgId);

        int base;

        if (lastVersion == null || lastVersion.getVersionId() == null) {
            return 11111;
        }

        base = lastVersion.getVersionId();

        int increment = new Random().nextInt(37) + 9;

        return base + increment;
    }

//    @Transactional
//    public OverlapRecordEntity saveOverlapRecords(OverlapRecordsRequest request) {
//
//        Long orgId = request.getOrganizationId();
//
//        OverlapRecordEntity existingRecord =
//                overlapRecordsRepository
//                        .findByOrganizationIdAndRecordType(orgId, request.getRecordType())
//                        .orElse(null);
//
//        Integer version;
//
//        if (existingRecord != null) {
//            version = existingRecord.getVersion() + 1;
//        } else {
//            version = 1;
//        }
//
//        Integer versionId = generateNextVersionId(orgId);
//
//        log.info("Processing overlap orgId={} version={} versionId={}",
//                orgId, version, versionId);
//
//        OverlapRecordEntity overlapRecordEntity;
//
//        if (existingRecord != null) {
//            overlapRecordEntity = existingRecord;
//        } else {
//            overlapRecordEntity = new OverlapRecordEntity();
//        }
//
//        overlapRecordEntity.setOrganizationId(orgId);
//        overlapRecordEntity.setRecordType(request.getRecordType());
//        overlapRecordEntity.setFileName(request.getFileName());
//        overlapRecordEntity.setSource(request.getSource());
//        overlapRecordEntity.setFrequency(request.getFrequency());
//        overlapRecordEntity.setGoogleSheetLink(request.getGoogleSheetLink());
//        overlapRecordEntity.setFieldToColumnMapping(request.getFieldToColumnMapping());
//
//        overlapRecordEntity.setVersion(version);
//        overlapRecordEntity.setVersionId(versionId);
//
//        List<String> websites = new ArrayList<>();
//
//        if (request.getFields() != null && !request.getFields().isEmpty()) {
//
//            overlapRecordEntity.getFields().clear();
//
//            List<OverlapRecordFieldEntity> fields = request.getFields()
//                    .stream()
//                    .map(fieldRequest -> {
//
//                        OverlapRecordFieldEntity field = new OverlapRecordFieldEntity();
//
//                        field.setName(fieldRequest.getName());
//                        field.setCompanyName(fieldRequest.getCompanyName());
//                        field.setContactEmail(fieldRequest.getContactEmail());
//                        field.setDomain(fieldRequest.getDomain());
//                        field.setDealStage(fieldRequest.getDealStage());
//                        field.setCreationDate(fieldRequest.getCreationDate());
//                        field.setCloseDate(fieldRequest.getCloseDate());
//                        field.setSubscribed(fieldRequest.getSubscribed());
//                        field.setTicketSize(fieldRequest.getTicketSize());
//
//                        field.setVersion(version);
//                        field.setVersionId(versionId);
//
//                        field.setOverlapRecord(overlapRecordEntity);
//
//                        websites.add(fieldRequest.getDomain());
//
//                        return field;
//
//                    }).toList();
//
//            overlapRecordEntity.getFields().addAll(fields);
//        }
//
//        var freq = Frequency.valueOf(request.getFrequency().name());
//
//        PersonaRequest personaRequest = new PersonaRequest(
//                orgId,
//                websites,
//                new String[]{},
//                freq,
//                "",
//                request.getFieldToColumnMapping().get("domain"),
//                request.getGoogleSheetLink(),
//                request.getSource(),
//                request.getFileName()
//        );
//
//        PersonaStatusEntity status = new PersonaStatusEntity();
//
//        status.setOrganizationId(orgId);
//        status.setVersion(version);
//        status.setVersionId(versionId);
//        status.setColumnName(personaRequest.getColumnName());
//        status.setGoogleSheetLink(personaRequest.getGoogleSheetLink());
//        status.setPersonaMode(personaRequest.getPersonaMode());
//        status.setFileName(personaRequest.getFileName());
//        status.setFrequency(personaRequest.getFrequency());
//        status.setPersonaStatus(PersonaStatus.INITIATED);
//
//        if (RecordType.CUSTOMER.equals(request.getRecordType())) {
//
//            personaStatusRepository.save(status);
//
//            asyncPersonaService.savePersona(personaRequest, version, versionId);
//        }
//
//        OverlapRecordEntity savedRecord = overlapRecordsRepository.save(overlapRecordEntity);
//
//        dynamicTableService.syncOverlapRecordToDynamicTable(savedRecord);
//
//        return savedRecord;
//    }

    @Transactional
    public OverlapRecordEntity saveOverlapRecords(OverlapRecordsRequest request) {

        Long orgId = request.getOrganizationId();

        OverlapRecordEntity lastRecord =
                overlapRecordsRepository
                        .findTopByOrganizationIdAndRecordTypeOrderByVersionDesc(
                                orgId, request.getRecordType())
                        .orElse(null);

        Integer version = (lastRecord != null) ? lastRecord.getVersion() + 1 : 1;

        Integer versionId = generateNextVersionId(orgId);

        log.info("Processing overlap orgId={} version={} versionId={}",
                orgId, version, versionId);

        // ALWAYS CREATE NEW ENTITY (NO UPDATE)
        OverlapRecordEntity overlapRecordEntity = new OverlapRecordEntity();

        overlapRecordEntity.setOrganizationId(orgId);
        overlapRecordEntity.setRecordType(request.getRecordType());
        overlapRecordEntity.setFileName(request.getFileName());
        overlapRecordEntity.setSource(request.getSource());
        overlapRecordEntity.setFrequency(request.getFrequency());
        overlapRecordEntity.setGoogleSheetLink(request.getGoogleSheetLink());
        overlapRecordEntity.setFieldToColumnMapping(request.getFieldToColumnMapping());

        overlapRecordEntity.setVersion(version);
        overlapRecordEntity.setVersionId(versionId);

        List<String> websites = new ArrayList<>();

        if (request.getFields() != null && !request.getFields().isEmpty()) {

            List<OverlapRecordFieldEntity> fields = request.getFields()
                    .stream()
                    .map(fieldRequest -> {

                        OverlapRecordFieldEntity field = new OverlapRecordFieldEntity();

                        field.setName(fieldRequest.getName());
                        field.setCompanyName(fieldRequest.getCompanyName());
                        field.setContactEmail(fieldRequest.getContactEmail());
                        field.setDomain(fieldRequest.getDomain());
                        field.setDealStage(fieldRequest.getDealStage());
                        field.setCreationDate(fieldRequest.getCreationDate());
                        field.setCloseDate(fieldRequest.getCloseDate());
                        field.setSubscribed(fieldRequest.getSubscribed());
                        field.setTicketSize(fieldRequest.getTicketSize());

                        field.setVersion(version);
                        field.setVersionId(versionId);

                        field.setOverlapRecord(overlapRecordEntity);

                        websites.add(fieldRequest.getDomain());

                        return field;

                    }).toList();

            overlapRecordEntity.getFields().addAll(fields);
        }

        var freq = Frequency.valueOf(request.getFrequency().name());

        PersonaRequest personaRequest = new PersonaRequest(
                orgId,
                websites,
                new String[]{},
                freq,
                "",
                request.getFieldToColumnMapping().get("domain"),
                request.getGoogleSheetLink(),
                request.getSource(),
                request.getFileName()
        );

        PersonaStatusEntity status = new PersonaStatusEntity();

        status.setOrganizationId(orgId);
        status.setVersion(version);
        status.setVersionId(versionId);
        status.setColumnName(personaRequest.getColumnName());
        status.setGoogleSheetLink(personaRequest.getGoogleSheetLink());
        status.setPersonaMode(personaRequest.getPersonaMode());
        status.setFileName(personaRequest.getFileName());
        status.setFrequency(personaRequest.getFrequency());
        status.setPersonaStatus(PersonaStatus.INITIATED);

        if (RecordType.CUSTOMER.equals(request.getRecordType())) {

            personaStatusRepository.save(status);

            asyncPersonaService.savePersona(personaRequest, version, versionId);
        }

        OverlapRecordEntity savedRecord = overlapRecordsRepository.save(overlapRecordEntity);

        dynamicTableService.syncOverlapRecordToDynamicTable(savedRecord);

        return savedRecord;
    }

    @Transactional
    public OverlapRecordEntity saveOverlapRecordsVersioning(OverlapRecordsRequest request) {

        Long orgId = request.getOrganizationId();

        OverlapRecordEntity lastRecord =
                overlapRecordsRepository
                        .findTopByOrganizationIdAndRecordTypeOrderByVersionDesc(
                                orgId, request.getRecordType())
                        .orElse(null);

        Integer version = (lastRecord != null) ? lastRecord.getVersion() + 1 : 1;

        Integer versionId = generateNextVersionId(orgId);

        log.info("Processing overlap orgId={} version={} versionId={}",
                orgId, version, versionId);

        // ALWAYS CREATE NEW ENTITY (NO UPDATE)
        OverlapRecordEntity overlapRecordEntity = new OverlapRecordEntity();

        overlapRecordEntity.setOrganizationId(orgId);
        overlapRecordEntity.setRecordType(request.getRecordType());
        overlapRecordEntity.setFileName(request.getFileName());
        overlapRecordEntity.setSource(request.getSource());
        overlapRecordEntity.setFrequency(request.getFrequency());
        overlapRecordEntity.setGoogleSheetLink(request.getGoogleSheetLink());
        overlapRecordEntity.setFieldToColumnMapping(request.getFieldToColumnMapping());

        overlapRecordEntity.setVersion(version);
        overlapRecordEntity.setVersionId(versionId);

        List<String> websites = new ArrayList<>();

        if (request.getFields() != null && !request.getFields().isEmpty()) {

            List<OverlapRecordFieldEntity> fields = request.getFields()
                    .stream()
                    .map(fieldRequest -> {

                        OverlapRecordFieldEntity field = new OverlapRecordFieldEntity();

                        field.setName(fieldRequest.getName());
                        field.setCompanyName(fieldRequest.getCompanyName());
                        field.setContactEmail(fieldRequest.getContactEmail());
                        field.setDomain(fieldRequest.getDomain());
                        field.setDealStage(fieldRequest.getDealStage());
                        field.setCreationDate(fieldRequest.getCreationDate());
                        field.setCloseDate(fieldRequest.getCloseDate());
                        field.setSubscribed(fieldRequest.getSubscribed());
                        field.setTicketSize(fieldRequest.getTicketSize());

                        field.setVersion(version);
                        field.setVersionId(versionId);

                        field.setOverlapRecord(overlapRecordEntity);

                        websites.add(fieldRequest.getDomain());

                        return field;

                    }).toList();

            overlapRecordEntity.getFields().addAll(fields);
        }

        var freq = Frequency.valueOf(request.getFrequency().name());

        PersonaRequest personaRequest = new PersonaRequest(
                orgId,
                websites,
                new String[]{},
                freq,
                "",
                request.getFieldToColumnMapping().get("domain"),
                request.getGoogleSheetLink(),
                request.getSource(),
                request.getFileName()
        );

        PersonaStatusEntity status = new PersonaStatusEntity();

        status.setOrganizationId(orgId);
        status.setVersion(version);
        status.setVersionId(versionId);
        status.setColumnName(personaRequest.getColumnName());
        status.setGoogleSheetLink(personaRequest.getGoogleSheetLink());
        status.setPersonaMode(personaRequest.getPersonaMode());
        status.setFileName(personaRequest.getFileName());
        status.setFrequency(personaRequest.getFrequency());
        status.setPersonaStatus(PersonaStatus.INITIATED);

        if (RecordType.CUSTOMER.equals(request.getRecordType())) {

            personaStatusRepository.save(status);

            asyncPersonaServiceVersioning.savePersona(personaRequest, version, versionId);
        }

        OverlapRecordEntity savedRecord = overlapRecordsRepository.save(overlapRecordEntity);

        dynamicTableService.syncOverlapRecordToDynamicTable(savedRecord);

        return savedRecord;
    }



    public List<OverlapRecordVersionResponse> getOverlapVersionsByOrgId(Long orgId,RecordType recordType) {

        log.info("[GET_OVERLAP_VERSIONS] orgId={}", orgId);

        List<OverlapRecordEntity> records =
                overlapRecordsRepository.findOverlapRecords(orgId,recordType);

        return records.stream()
                .map(record -> new OverlapRecordVersionResponse(
                        record.getVersion(),
                        record.getVersionId()
                ))
                .toList();
    }

    public List<OverlapRecordEntity> getOverlapRecordsV2(RecordType recordType, Integer versionId) {

        Long organizationId = Util.getOrgIdFromToken();

        if (recordType != null && versionId != null) {
            return overlapRecordsRepository
                    .findByOrganizationIdAndRecordTypeAndVersionId(
                            organizationId, recordType, versionId);

        } else if (recordType != null) {
            var result = overlapRecordsRepository
                    .findByOrganizationIdAndRecordType(organizationId, recordType);

            return result.map(List::of).orElseGet(ArrayList::new);

        } else if (versionId != null) {
            return overlapRecordsRepository
                    .findByOrganizationIdAndVersionId(organizationId, versionId);

        } else {
            return overlapRecordsRepository.findByOrganizationId(organizationId);
        }
    }
}
