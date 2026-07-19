package com.sharkdom.offlinePartner.service;

import com.sharkdom.constants.Constants;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.emailOutreach.entity.EmailAccount;
import com.sharkdom.emailOutreach.repository.EmailAccountRepository;
import com.sharkdom.entity.externalpartner.ExternalPartnerAssignment;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.exception.SharkdomException;
import com.sharkdom.model.PersonaStatus;
import com.sharkdom.model.ai.PercentageCategory;
import com.sharkdom.model.ai.PersonaModelResponse;
import com.sharkdom.mypartner.dto.PaginatedResponse;
import com.sharkdom.offlinePartner.entity.*;
import com.sharkdom.offlinePartner.model.*;
import com.sharkdom.offlinePartner.repository.*;
import com.sharkdom.repository.email.EmailStatisticsRepository;
import com.sharkdom.repository.externalpartner.ExternalPartnerAssignmentRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.ai.PersonaService;
import com.sharkdom.service.email.AmazonSes;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.tablecustomization.service.externalpartner.ExternalPartnerTableService;
import com.sharkdom.util.AzureStorageService;
import com.sharkdom.util.UniqueCodeGenerator;
import com.sharkdom.util.Util;
import com.sharkdom.zoho.entity.ZohoSignedDocumentEntity;
import com.sharkdom.zoho.service.ZohoDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sharkdom.service.organization.OrganizationService.ALGORITHM;

@Service
@Slf4j
public class OfflinePartnerService {
    @Value("${env}")
    private String env;
    private final RestTemplate restTemplate;
    @Value("${persona.url}")
    private String personaUrl;
    private final EmailService emailService;
    private final AmazonSes amazonSes;
    private final OfflinePartnerInviteRepository offlinePartnerInviteRepository;
    private final OrganizationRepository organizationRepository;
    private final EmailStatisticsRepository emailStatisticsRepository;
    private final AzureStorageService azureStorageService;
    private final OfflinePartnerDocumentsRepository offlinePartnerDocumentsRepository;
    private final OfflinePersonaRepository offlinePersonaRepository;
    private final OfflinePersonaStatusRepository offlinePersonaStatusRepository;
    private final OfflinePersonaDetailsRepository offlinePersonaDetailsRepository;
    private final PersonaService personaService;
    private final ZohoDocumentService zohoDocumentService;
    private final ExternalPartnerAssignmentRepository externalPartnerAssignmentRepository;
    private final DynamicTableService dynamicTableService;
    private final DynamicTableRepository dynamicTableRepository;
    private final ExternalPartnerTableService externalPartnerTableService;

    @Autowired
    private EmailAccountRepository emailAccountRepository;

    public OfflinePartnerService(EmailService emailService, AmazonSes amazonSes, OfflinePartnerInviteRepository offlinePartnerInviteRepository, OrganizationRepository organizationRepository, EmailStatisticsRepository emailStatisticsRepository, AzureStorageService azureStorageService, OfflinePartnerDocumentsRepository offlinePartnerDocumentsRepository, OfflinePersonaRepository offlinePersonaRepository, OfflinePersonaStatusRepository offlinePersonaStatusRepository, OfflinePersonaDetailsRepository offlinePersonaDetailsRepository, PersonaService personaService, ZohoDocumentService zohoDocumentService, ExternalPartnerAssignmentRepository externalPartnerAssignmentRepository, DynamicTableService dynamicTableService, DynamicTableRepository dynamicTableRepository, ExternalPartnerTableService externalPartnerTableService) {
        this.emailService = emailService;
        this.amazonSes = amazonSes;
        this.offlinePartnerInviteRepository = offlinePartnerInviteRepository;
        this.organizationRepository = organizationRepository;
        this.emailStatisticsRepository = emailStatisticsRepository;
        this.azureStorageService = azureStorageService;
        this.offlinePartnerDocumentsRepository = offlinePartnerDocumentsRepository;
        this.offlinePersonaRepository = offlinePersonaRepository;
        this.offlinePersonaStatusRepository = offlinePersonaStatusRepository;
        this.offlinePersonaDetailsRepository = offlinePersonaDetailsRepository;
        this.personaService = personaService;
        this.zohoDocumentService = zohoDocumentService;
        this.externalPartnerAssignmentRepository = externalPartnerAssignmentRepository;
        this.dynamicTableService = dynamicTableService;
        this.dynamicTableRepository = dynamicTableRepository;
        this.externalPartnerTableService = externalPartnerTableService;
        restTemplate = new RestTemplate();
    }

    public void save(OfflinePartnerSaveRequest offlinePartnerSaveRequest) {
        offlinePartnerSaveRequest.partnerInviteDetails().forEach(offlinePartnerInviteDetail -> {
            offlinePartnerInviteRepository.findByOrganizationIdAndEmail(offlinePartnerSaveRequest.organizationId(), offlinePartnerInviteDetail.email()).ifPresent(offlinePartnerInvite -> {
                throw new SharkdomException(ErrorMessages.SH87);
            });
            String uniqueCode = UniqueCodeGenerator.generateUniqueString();
            var offlinePartnerInvite = OfflinePartnerInvite.builder()
                    .organizationId(offlinePartnerSaveRequest.organizationId())
                    .email(offlinePartnerInviteDetail.email())
                    .isMember(offlinePartnerInviteDetail.isMember())
                    .partnerName(offlinePartnerInviteDetail.partnerName())
                    .status(PartnerInviteStatus.INVITE_NOT_SENT.name())
                    .offlinePartnerMessageCode(uniqueCode)
                    .remarks(offlinePartnerInviteDetail.remarks())
                    .build();
            var save = offlinePartnerInviteRepository.save(offlinePartnerInvite);
            externalPartnerTableService.processOfflinePartnerRow(
                    offlinePartnerSaveRequest.organizationId(),
                    save
            );
        });
    }

    public List<Map<String, String>> invitePartners(OfflinePartnerInviteRequest offlinePartnerInviteRequest) {
        List<OfflinePartnerInvite> invites;
        if (offlinePartnerInviteRequest.sendAll()) {
            invites = offlinePartnerInviteRepository.findByOrganizationId(offlinePartnerInviteRequest.organizationId());
        } else {
            invites = offlinePartnerInviteRepository.findByOrganizationIdAndEmailIn(offlinePartnerInviteRequest.organizationId(), offlinePartnerInviteRequest.emails());
        }
        return invites.stream().map(partnerInvite -> {
            String userId = RandomStringUtils.random(10, true, true);
            String encode = userId + ":" + offlinePartnerInviteRequest.organizationId() + ":" + "role" + ":" + partnerInvite.getEmail();
            var encodedValue = encrypt(encode);
            String url;
            if (env.equalsIgnoreCase("dev")) {
                url = "https://dev.sharkdom.com/onboarding?utm_register=" + encodedValue;
            } else {
                url = "https://www.sharkdom.com/onboarding?utm_register=" + encodedValue;
            }
            emailService.invitePartner("Partner_invite", partnerInvite.getEmail(), url, "", offlinePartnerInviteRequest.organizationId());
            partnerInvite.setStatus(PartnerInviteStatus.INVITE_SENT.name());
            offlinePartnerInviteRepository.save(partnerInvite);
            return Map.of("signupUrl", url);
        }).collect(Collectors.toList());

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

    public void deletePartners(String email) {
        Long organizationId = Util.getOrgIdFromToken();
        offlinePartnerInviteRepository.findByOrganizationIdAndEmail(organizationId, email).ifPresent(offlinePartnerInviteRepository::delete);
    }

    public List<OfflinePartnerInvite> groupPartners(GroupPartnerRequest groupPartnerRequest) {
        return offlinePartnerInviteRepository.findByOrganizationIdAndEmailIn(groupPartnerRequest.organizationId(), groupPartnerRequest.emails()).stream().map(partnerInvite -> {
            partnerInvite.setPartnerGroup(groupPartnerRequest.partnerGroup());
            return offlinePartnerInviteRepository.save(partnerInvite);
        }).toList();
    }

    public List<OfflinePartnerInvite> getOfflinePartners(PartnerInviteStatus status) {
        Long organizationId = Util.getOrgIdFromToken();
        if (status == null || status.equals(PartnerInviteStatus.ALL)) {
            return offlinePartnerInviteRepository.findByOrganizationId(organizationId);
        }
        List<OfflinePartnerInvite> offlinePartnerInvites = offlinePartnerInviteRepository.findByOrganizationIdAndStatus(organizationId, status.name());
        for (OfflinePartnerInvite offlinePartnerInvite:offlinePartnerInvites)
        {
            Optional<ExternalPartnerAssignment> repository = externalPartnerAssignmentRepository.findByOrganizationIdAndExternalPartnerId(organizationId, offlinePartnerInvite.getId());
            repository.ifPresent(externalPartnerAssignment -> offlinePartnerInvite.setUserId(externalPartnerAssignment.getUserId()));
        }
        return offlinePartnerInvites;
    }

    public OfflinePartnerInvite sendVerificationEmail(SendVerificationEmailRequest sendVerificationEmailRequest) {
        var partnerDetails = offlinePartnerInviteRepository.findByOrganizationIdAndEmail(sendVerificationEmailRequest.organizationId(), sendVerificationEmailRequest.email());
        return partnerDetails.map(offlinePartnerInvite -> {
            var organizationName = organizationRepository.findNameById(sendVerificationEmailRequest.organizationId());
            organizationName = organizationName.replace(" ", ".");
            String senderEmail = String.format("%s@sharkdom.com", organizationName);
            var sender = String.format("%s via SharkDom <%s>", organizationName, senderEmail);
            try {
                amazonSes.prepareAndSend(sendVerificationEmailRequest.subject(), sendVerificationEmailRequest.body(),
                        null,
                        sender,
                        sendVerificationEmailRequest.email(),
                        null,
                        "PARTNER_VERIFICATION_EMAIL",
                        sendVerificationEmailRequest.organizationId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            offlinePartnerInvite.setVerifyEmailSent(true);
            return offlinePartnerInviteRepository.save(offlinePartnerInvite);
        }).orElseThrow(() -> new SharkdomException(ErrorMessages.SH88, sendVerificationEmailRequest.email()));

    }

    public OfflinePartnerInviteResponse getPartnerDetailsById(Long id) {
        var partners = offlinePartnerInviteRepository.findById(id);
        return partners.map(partnerInvite -> {
            var optionalOrganization = organizationRepository.findByPrimaryEmail(partnerInvite.getEmail());
            var partnerInviteResponse = OfflinePartnerInviteResponse.builder();
            partnerInviteResponse.email(partnerInvite.getEmail());
            partnerInviteResponse.id(partnerInvite.getId());
            partnerInviteResponse.remarks(partnerInvite.getRemarks());
            Optional<EmailAccount> emailAccount = emailAccountRepository.findByOrganizationId(Util.getOrgIdFromToken());
            if (emailAccount.isPresent())
            {
                partnerInviteResponse.isMailBoxClaimed(true);
            }
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
                partnerInviteResponse.name(partnerInvite.getPartnerName());
            }
            partnerInviteResponse.inviteEmailOpened(emailStatisticsRepository.existsByEventTypeAndEnvAndEmailAndTemplateCode("Open", env, partnerInvite.getEmail(), "Partner_invite"));
            partnerInviteResponse.inviteEmailClicked(emailStatisticsRepository.existsByEventTypeAndEnvAndEmailAndTemplateCode("CLick", env, partnerInvite.getEmail(), "Partner_invite"));
            partnerInviteResponse.verifyEmailOpened(emailStatisticsRepository.existsByEventTypeAndEnvAndEmailAndTemplateCode("Open", env, partnerInvite.getEmail(), "PARTNER_VERIFICATION_EMAIL"));
            partnerInviteResponse.verifyEmailClicked(emailStatisticsRepository.existsByEventTypeAndEnvAndEmailAndTemplateCode("CLick", env, partnerInvite.getEmail(), "PARTNER_VERIFICATION_EMAIL"));

            return partnerInviteResponse.build();
        }).orElseThrow(() -> new SharkdomException(ErrorMessages.SH89, id));
    }

    public OfflinePartnerInviteResponse getPartnerDetailsByExternalPartnerCode(String externalPartnerCode) {
        var partners = offlinePartnerInviteRepository.findByOfflinePartnerMessageCode(externalPartnerCode);
        return partners.map(partnerInvite -> {
            var optionalOrganization = organizationRepository.findByPrimaryEmail(partnerInvite.getEmail());
            var partnerInviteResponse = OfflinePartnerInviteResponse.builder();
            partnerInviteResponse.email(partnerInvite.getEmail());
            partnerInviteResponse.id(partnerInvite.getId());
            partnerInviteResponse.remarks(partnerInvite.getRemarks());
            Optional<EmailAccount> emailAccount = emailAccountRepository.findByOrganizationId(Util.getOrgIdFromToken());
            if (emailAccount.isPresent())
            {
                partnerInviteResponse.isMailBoxClaimed(true);
            }
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
                partnerInviteResponse.name(partnerInvite.getPartnerName());
            }
            partnerInviteResponse.inviteEmailOpened(emailStatisticsRepository.existsByEventTypeAndEnvAndEmailAndTemplateCode("Open", env, partnerInvite.getEmail(), "Partner_invite"));
            partnerInviteResponse.inviteEmailClicked(emailStatisticsRepository.existsByEventTypeAndEnvAndEmailAndTemplateCode("CLick", env, partnerInvite.getEmail(), "Partner_invite"));
            partnerInviteResponse.verifyEmailOpened(emailStatisticsRepository.existsByEventTypeAndEnvAndEmailAndTemplateCode("Open", env, partnerInvite.getEmail(), "PARTNER_VERIFICATION_EMAIL"));
            partnerInviteResponse.verifyEmailClicked(emailStatisticsRepository.existsByEventTypeAndEnvAndEmailAndTemplateCode("CLick", env, partnerInvite.getEmail(), "PARTNER_VERIFICATION_EMAIL"));

            return partnerInviteResponse.build();
        }).orElseThrow(() -> new SharkdomException(ErrorMessages.SH89, externalPartnerCode));
    }

    public OfflinePartnerDocuments uploadFile(MultipartFile pdf, OfflinePartnerDocumentRequest request) {
        try {
            var orgId = Util.getOrgIdFromToken();
            var pdfPath = "/" + env + "/offlinepartner/" + request.getOrganizationId() + "/" + request.getEmail() + "/" + pdf.getOriginalFilename().replaceAll(".pdf", "") + ".pdf";
            var pdfLink = azureStorageService.uploadFile(pdf.getInputStream(), pdfPath);

            Optional<DynamicTable> optionalDynamicTable=dynamicTableRepository.findByOrgIdAndEmail(orgId, request.getEmail());
            if (optionalDynamicTable.isPresent()) {
                DynamicTable dynamicTable=optionalDynamicTable.get();
                dynamicTableService.createRow(dynamicTable.getId(), pdfLink);
            }

            var offlineDocument = OfflinePartnerDocuments.builder()
                    .email(request.getEmail())
                    .organizationId(request.getOrganizationId())
                    .pdfUrl(pdfPath)
                    .docId(request.getDocId())
                    .count(request.getCount())
                    .effectiveDate(request.getEffectiveDate())
                    .expiringDate(request.getExpiringDate())
                    .build();
            return offlinePartnerDocumentsRepository.save(offlineDocument);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<OfflinePartnerDocuments> getContract(String email) {
        Long organizationId = Util.getOrgIdFromToken();
        return offlinePartnerDocumentsRepository.findAllByOrganizationIdAndEmail(organizationId, email);
    }

    public OfflinePartnerInvite updatePartnerDetails(UpdatePartnerDetail request) {
        return offlinePartnerInviteRepository.findByOrganizationIdAndEmail(request.organizationId(), request.email()).map(offlinePartnerInvite -> {
            if (request.remarks() != null) {
                offlinePartnerInvite.setRemarks(request.remarks());
            }
            if (request.partnerName() != null) {
                offlinePartnerInvite.setPartnerName(request.partnerName());
            }
            return offlinePartnerInviteRepository.save(offlinePartnerInvite);
        }).orElseThrow(() -> new SharkdomException(ErrorMessages.SH89, request.organizationId()));
    }

    @Async
    @Transactional
    public void savePersona(PartnerPersonaRequest personaRequest) {
        callPersona(personaRequest);
    }

    public void callPersona(PartnerPersonaRequest personaRequest) {
        log.info("calling persona");
        Map<String, List<String>> postRequest = new HashMap<>();
        postRequest.put("sites", personaRequest.getSites());
        HttpEntity<Map<String, List<String>>> requestEntity = new HttpEntity<>(postRequest);
        ParameterizedTypeReference<List<com.sharkdom.model.ai.PersonaModelResponse>> responseType = new ParameterizedTypeReference<>() {
        };
        List<PersonaModelResponse> responseEntity = restTemplate.exchange(personaUrl, HttpMethod.POST, requestEntity, responseType).getBody();
        savePersonaDetails(responseEntity, personaRequest.getOrganizationId(), personaRequest.getPartnerEmail());
        if (responseEntity != null) {
            calculateAndSavePercentages(responseEntity, personaRequest.getOrganizationId(), personaRequest.getPartnerEmail());
        }
    }

    private void savePersonaDetails(List<PersonaModelResponse> responseEntity, Long organizationId, String partnerEmail) {
        if (responseEntity != null) {
            List<OfflinePersonaDetailsEntity> personaDetails = responseEntity.stream().map(response -> OfflinePersonaDetailsEntity.builder()
                    .companySector(response.getCompanySector())
                    .isPartnershipProgram(response.getIsPartnershipProgram())
                    .organizationId(organizationId)
                    .marketSegment(response.getMarketSegment())
                    .partnerEmail(partnerEmail)
                    .companySize(response.getCompanySize())
                    .build()).toList();
            offlinePersonaDetailsRepository.saveAll(personaDetails);
        }
    }

    public void calculateAndSavePercentages(List<PersonaModelResponse> personaModelResponses, Long organizationId, String partnerEmail) {
        Map<String, List<PercentageCategory>> percentages = new HashMap<>();
        int totalCount = personaModelResponses.size();

        percentages.put("companySector", calculatePercentage(personaModelResponses, PersonaModelResponse::getCompanySector, totalCount));
        percentages.put("companySize", calculatePercentage(personaModelResponses, PersonaModelResponse::getCompanySize, totalCount));
        percentages.put("isPartnershipProgram", calculatePercentage(personaModelResponses, PersonaModelResponse::getIsPartnershipProgram, totalCount));
        percentages.put("marketSegment", calculatePercentage(personaModelResponses, PersonaModelResponse::getMarketSegment, totalCount));

        // Save to database
        savePercentagesToDb(percentages, organizationId, partnerEmail);

    }

    private List<PercentageCategory> calculatePercentage(List<PersonaModelResponse> personaModelResponses, Function<PersonaModelResponse, String> classifier, int totalCount) {
        Map<String, Long> counts = personaModelResponses.stream()
                .collect(Collectors.groupingBy(classifier, Collectors.counting()));

        return counts.entrySet().stream()
                .map(entry -> new PercentageCategory(entry.getKey(), (entry.getValue() * 100.0) / totalCount))
                .collect(Collectors.toList());
    }

    private void savePercentagesToDb(Map<String, List<PercentageCategory>> percentages, Long organizationId, String partnerEmail) {
        for (Map.Entry<String, List<PercentageCategory>> entry : percentages.entrySet()) {
            String attribute = entry.getKey();
            for (PercentageCategory categoryEntry : entry.getValue()) {
                String category = categoryEntry.getKey();
                Double percentage = categoryEntry.getPercentage();
                OfflinePersonaEntity record = OfflinePersonaEntity.builder().
                        organizationId(organizationId)
                        .partnerEmail(partnerEmail)
                        .category(category).percentage(percentage).attribute(attribute).build();
                offlinePersonaRepository.save(record);
            }
        }
        var response = offlinePersonaStatusRepository.getByOrganizationIdAndPartnerEmail(organizationId, partnerEmail);
        if (response != null) {
            response.setPersonaStatus(PersonaStatus.COMPLETED);
            offlinePersonaStatusRepository.save(response);
        }

    }

    public Map<String, List<PercentageCategory>> getAllData(Long organizationId, String partnerEmail) {
        List<OfflinePersonaEntity> records = offlinePersonaRepository.getAllByOrganizationIdAndPartnerEmail(organizationId, partnerEmail);

        Map<String, List<PercentageCategory>> response = new HashMap<>();

        records.forEach(record -> response
                .computeIfAbsent(record.getAttribute(), k -> new java.util.ArrayList<>())
                .add(new PercentageCategory(record.getCategory(), record.getPercentage()))
        );
        return response;
    }

    public OfflinePersonaResponse getPersonaDetails(String partnerEmail, int page, int size) {
        Long organizationId = Util.getOrgIdFromToken();
        var statusResponse = offlinePersonaStatusRepository.getByOrganizationIdAndPartnerEmail(organizationId, partnerEmail);
        var response = offlinePersonaDetailsRepository.getAllByOrganizationIdAndPartnerEmail(organizationId, partnerEmail, PageRequest.of(page, size));
        var details = getAllData(organizationId, partnerEmail);
        var organizationDetails = personaService.getPersonaDetails(page, size);
        var builder = OfflinePersonaResponse.builder()
                .organizationPersonaCompleted(!organizationDetails.getPersonaDetails().getContent().isEmpty())
                .partnerPersonaCompleted(!response.getContent().isEmpty())
                .organizationPersonaDetails(organizationDetails.getPersonaDetails())
                .partnerPersonaDetails(response)
                .partnerCategory(details)
                .organizationCategory(organizationDetails.getCategory());

        if (Objects.nonNull(statusResponse)) {
            builder.personaStatus(statusResponse.getPersonaStatus()).creationTimestamp(statusResponse.getCreationTimestamp());
        }
        return builder.build();
    }

    public void signOfflinePartnerDocument(String partnerEmail, MultipartFile file) {
        Long organizationId = Util.getOrgIdFromToken();
        var offlinePartnersOptional = offlinePartnerInviteRepository.findByOrganizationIdAndEmail(organizationId, partnerEmail);
        if (offlinePartnersOptional.isPresent()) {
            var offlinePartners = offlinePartnersOptional.get();
            zohoDocumentService.signOfflinePartnerDocument(organizationId, offlinePartners.getId(),offlinePartners.getOfflinePartnerMessageCode(), file);
        }
    }

    public List<ZohoSignedDocumentEntity> getOfflineZohoDocuments() {
        Long organizationId = Util.getOrgIdFromToken();
        return offlinePartnerInviteRepository.findByOrganizationId(organizationId).stream()
                .flatMap(offlinePartnerInvite -> zohoDocumentService.getOfflineZohoDocuments(offlinePartnerInvite.getId()).stream())
                .toList();
    }

    public PaginatedResponse<OfflinePartnerInvite> getOfflinePartnerList(PartnerInviteStatus status, int page, int size, String sortBy, String sortDir) {
        Long organizationId = Util.getOrgIdFromToken();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OfflinePartnerInvite> pageResult;

        // Apply status filter
        if (status == null || status.equals(PartnerInviteStatus.ALL)) {
            pageResult = offlinePartnerInviteRepository.getOfflinePartnersByOrgId(organizationId, pageable);
        } else {
            pageResult = offlinePartnerInviteRepository.getOfflinePartnersByOrgIdAndStatus(organizationId, status.name(), pageable);
        }

        List<OfflinePartnerInvite> content = pageResult.getContent();

        // Attach userId from ExternalPartnerAssignment
        for (OfflinePartnerInvite offlinePartnerInvite : content) {
            externalPartnerAssignmentRepository
                    .findByOrganizationIdAndExternalPartnerId(organizationId, offlinePartnerInvite.getId())
                    .ifPresent(assignment -> offlinePartnerInvite.setUserId(assignment.getUserId()));
        }

        return new PaginatedResponse<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    @Transactional
    public OfflinePartnerDocuments uploadFileV1(
            MultipartFile pdf,
            OfflinePartnerDocumentRequest request) {

        try {
            Long orgId = Util.getOrgIdFromToken();

            String pdfPath = "/" + env + "/offlinepartner/"
                    + request.getOrganizationId() + "/"
                    + request.getEmail() + "/"
                    + pdf.getOriginalFilename().replaceAll(".pdf", "")
                    + ".pdf";

            String pdfLink = azureStorageService
                    .uploadFile(pdf.getInputStream(), pdfPath);

            // 1. Find or create Dynamic Table
            DynamicTable table = dynamicTableRepository
                    .findByOrgIdAndEmail(orgId, request.getEmail())
                    .orElseGet(() ->
                            dynamicTableRepository.save(
                                    DynamicTable.builder()
                                            .orgId(orgId)
                                            .email(request.getEmail())
                                            .name("Offline Partner Documents")
                                            .build()
                            )
                    );

            // 2. Ensure default columns exist
            dynamicTableService.ensureDefaultColumns(table);

            // 3. Create row
            TableRow row = dynamicTableService.createRow(
                    table.getId(),
                    request.getDocId()
            );

            // 4. Save column values
            dynamicTableService.saveColumnValues(row, request, pdfLink);

            // 5. Save OfflinePartnerDocuments entity
            OfflinePartnerDocuments offlineDocument =
                    OfflinePartnerDocuments.builder()
                            .email(request.getEmail())
                            .organizationId(request.getOrganizationId())
                            .pdfUrl(pdfPath)
                            .docId(request.getDocId())
                            .count(request.getCount())
                            .effectiveDate(request.getEffectiveDate())
                            .expiringDate(request.getExpiringDate())
                            .build();

            return offlinePartnerDocumentsRepository.save(offlineDocument);

        } catch (Exception e) {
            throw new RuntimeException("Offline partner document upload failed", e);
        }
    }

    public String getOfflinePartnerMessageCodeByEmail(String email) {
        Long orgId = Util.getOrgIdFromToken();
        log.info("Fetching offlinePartnerMessageCode for email: {} and orgId: {}", email, orgId);
        OfflinePartnerInvite invite = offlinePartnerInviteRepository
                .findByEmailAndOrganizationId(email, orgId)
                .orElseGet(() -> createInviteForEmail(email, orgId));
        if (invite.getOfflinePartnerMessageCode() == null) {
            throw new ServiceException(ErrorMessages.SH106);
        }

        return invite.getOfflinePartnerMessageCode();
    }

    private OfflinePartnerInvite createInviteForEmail(String email, Long orgId) {
        log.info("No PartnerInvite found for email: {} and orgId: {}. Creating a new invite.", email, orgId);
        OfflinePartnerInvite invite = OfflinePartnerInvite.builder()
                .organizationId(orgId)
                .email(email)
                .status(PartnerInviteStatus.INVITE_NOT_SENT.name())
                .offlinePartnerMessageCode(UniqueCodeGenerator.generateUniqueString())
                .build();
        OfflinePartnerInvite saved = offlinePartnerInviteRepository.save(invite);
        externalPartnerTableService.processOfflinePartnerRow(orgId, saved);
        return saved;
    }

    public Map<String, String> inviteExternalPartner(ExternalPartnerInviteRequest request) {

        // Generate random userId
        String userId = RandomStringUtils.random(10, true, true);

        String rawData = userId + ":" +
                request.organizationId() + ":" +
                "role" + ":" +
                request.email();

        String encodedValue = encrypt(rawData);

        String url = env.equalsIgnoreCase("dev")
                ? "https://dev.sharkdom.com/onboarding?utm_register=" + encodedValue
                : "https://sharkdom.com/onboarding?utm_register=" + encodedValue;

        // Send Email (pass name now)
        emailService.invitePartner(
                "Partner_invite",
                request.email(),
                url,
                request.name(),
                request.organizationId()
        );

        return Map.of("signupUrl", url);
    }
}
