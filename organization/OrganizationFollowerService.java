package com.sharkdom.service.organization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.sharkdom.config.AppProperties;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.organization.OrganizationFollower;
import com.sharkdom.entity.organization.OrganizationFollowerHistory;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.organization.OrgEmailHistoryResponse;
import com.sharkdom.repository.organization.OrganizationFollowerHistoryRepository;
import com.sharkdom.repository.organization.OrganizationFollowerRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.email.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.sharkdom.constants.Constants.FOLLOWING_TEMPLATE;


@Service
public class OrganizationFollowerService {
    private OrganizationFollowerRepository organizationFollowerRepository;
    private OrganizationFollowerHistoryRepository organizationFollowerHistoryRepository;
    private ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final EmailService emailService;
    private final OrganizationRepository organizationRepository;

    public OrganizationFollowerService(OrganizationFollowerRepository organizationFollowerRepository, OrganizationFollowerHistoryRepository organizationFollowerHistoryRepository, ObjectMapper objectMapper, AppProperties appProperties, EmailService emailService, OrganizationRepository organizationRepository) {
        this.organizationFollowerRepository = organizationFollowerRepository;
        this.organizationFollowerHistoryRepository = organizationFollowerHistoryRepository;
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
        this.emailService = emailService;
        this.organizationRepository = organizationRepository;
    }


    public Page<OrganizationFollower> findByOrganizationId(long organizationId, int page, int size) {
        return organizationFollowerRepository.findAllByOrganizationId(organizationId, PageRequest.of(page, size));
    }

    public Page<OrganizationFollower> findByFollowerId(long followerOrganizationId, int page, int size) {
        return organizationFollowerRepository.findAllByFollowerOrganizationId(followerOrganizationId, PageRequest.of(page, size));
    }

    @Transactional
    public OrganizationFollower update(OrganizationFollower updated) throws Exception {
        findById(updated.getId());
        return organizationFollowerRepository.save(updated);
    }

    public OrganizationFollower findById(long id) {
        return organizationFollowerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH24, id));
    }

    public boolean findBooleanValueByOrganizationIdAndFollowerOrganizationId(long organizationId, long followerOrganizationId) {
        Optional<OrganizationFollower> organizationFollower = organizationFollowerRepository
                .findByOrganizationIdAndFollowerOrganizationId(organizationId,
                        followerOrganizationId);
        if (null == organizationFollower) {
            return false;
        } else return true;
    }

    public boolean checkRelationBetweenOrganization(long orgIdA, long orgIdB) {
        return organizationFollowerRepository.existsByOrganizationIdAndFollowerOrganizationIdOrFollowerOrganizationIdAndOrganizationId(orgIdA, orgIdB, orgIdA, orgIdB);
    }

    public OrganizationFollower findByOrganizationIdAndFollowerOrganizationId(long organizationId, long followerOrganizationId) {
        return organizationFollowerRepository
                .findByOrganizationIdAndFollowerOrganizationId(organizationId,
                        followerOrganizationId).orElseThrow(() ->
                        new ResourceNotFoundException(ErrorMessages.SH25, String.valueOf(organizationId), String.valueOf(followerOrganizationId)));
    }

    @Transactional
    public OrganizationFollower create(OrganizationFollower organizationFollower) {
        OrganizationFollower response = organizationFollowerRepository.save(organizationFollower);
        String followingName = organizationRepository.findNameById(organizationFollower.getOrganizationId());
        String followerName = organizationRepository.findNameById(organizationFollower.getFollowerOrganizationId());
        OrgEmailHistoryResponse first = organizationRepository.getOrgNameAndDescription(List.of(organizationFollower.getOrganizationId(), organizationFollower.getFollowerOrganizationId()));
        OrgEmailHistoryResponse second = organizationRepository.getOrgNameAndDescription(List.of(organizationFollower.getOrganizationId(), organizationFollower.getFollowerOrganizationId(), first.getId()));
        String templateCode = appProperties.getEmailTemplateCodeForEvent(FOLLOWING_TEMPLATE);
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/YYYY");
        var formattedDate = formatter.format(date);
        emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder()
                .templateCode(templateCode)
                .followingDate(formattedDate)
                .followingOrganizationName(followingName)
                .followerOrganizationName(followerName)
                .organization1Name(first.getName())
                .organization2Name(second.getName())
                .organizationIds(List.of(organizationFollower.getOrganizationId())).build(), null, 1L, 1L);
        return response;
    }

    @Transactional
    public OrganizationFollower patchByFollowerId(long organizationId, long followerOrganizationId, JsonPatch patch) throws Exception {
        OrganizationFollower optionalOrganizationFollower = findByOrganizationIdAndFollowerOrganizationId(organizationId, followerOrganizationId);
        OrganizationFollower organizationPatched = applyPatchToOrganizationFollower(patch, optionalOrganizationFollower);
        return organizationFollowerRepository.save(organizationPatched);
    }

    private OrganizationFollower applyPatchToOrganizationFollower(JsonPatch patch, OrganizationFollower targetUser)
            throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(targetUser, JsonNode.class));
        return objectMapper.treeToValue(patched, OrganizationFollower.class);
    }

    @Transactional
    public HttpStatus delete(long organizationId, long followerOrganizationId, String followStoppedByUserId) {
        OrganizationFollower organizationFollower = findByOrganizationIdAndFollowerOrganizationId(organizationId, followerOrganizationId);
        OrganizationFollowerHistory organizationFollowerHistory = new OrganizationFollowerHistory(organizationFollower, followStoppedByUserId);
        organizationFollowerRepository.delete(organizationFollower);
        organizationFollowerHistoryRepository.save(organizationFollowerHistory);
        return HttpStatus.OK;
    }
}
