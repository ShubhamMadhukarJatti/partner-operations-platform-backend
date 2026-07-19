package com.sharkdom.pipedrive.service.impl;


import com.sharkdom.constants.organization.IntegrationType;
import com.sharkdom.pipedrive.dto.CreatePersonRequest;
import com.sharkdom.pipedrive.dto.CreatePersonResponse;
import com.sharkdom.pipedrive.dto.PersonFieldsResponse;
import com.sharkdom.pipedrive.dto.PersonsResponse;
import com.sharkdom.pipedrive.service.*;
import com.sharkdom.repository.organization.IntegrationRepository;
import com.sharkdom.util.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Slf4j
@Service
public class PipeDriveServiceImpl implements PipeDriveService {

    @Autowired
    private IntegrationRepository integrationRepository;

    @Autowired
    private PipeDriveAuthService  pipeDriveAuthService;

    @Autowired
    private PipedrivePersonFieldService pipedrivePersonFieldService;

    @Autowired
    private PipedrivePersonService pipedrivePersonService;

    @Autowired
    private PipedriveCreatePersonService pipedriveCreatePersonService;

    @Transactional
    public PersonFieldsResponse getContacts() {
        log.info("getContacts");
        Long organizationId = Util.getOrgIdFromToken();
        log.info("organizationId: {}", organizationId);
        var details = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.PIPEDRIVE);
        var refreshToken = details.getRefreshToken();
        var accessToken = pipeDriveAuthService.refreshAccessToken(refreshToken);
        return pipedrivePersonFieldService.getPersonFields(accessToken.getAccessToken());
    }

    @Transactional
    public PersonsResponse getDetails() {
        log.info("Getting Person Details");
        Long organizationId = Util.getOrgIdFromToken();
        log.info("organizationId: {}", organizationId);
        var details = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.PIPEDRIVE);
        var refreshToken = details.getRefreshToken();
        var accessToken = pipeDriveAuthService.refreshAccessToken(refreshToken);
        return pipedrivePersonService.getPersons(accessToken.getAccessToken());
    }

    @Transactional
    public CreatePersonResponse createPerson(CreatePersonRequest createPersonRequest) {
        log.info("Creating person with name {}", createPersonRequest.getName());
        Long organizationId = Util.getOrgIdFromToken();
        log.info("organizationId: {}", organizationId);
        var details = integrationRepository.findByOrganizationIdAndIntegrationType(organizationId, IntegrationType.PIPEDRIVE);
        var refreshToken = details.getRefreshToken();
        var accessToken = pipeDriveAuthService.refreshAccessToken(refreshToken);
        return pipedriveCreatePersonService.createPerson(createPersonRequest,accessToken.getAccessToken());
    }

    @Transactional
    public CreatePersonResponse createPersonByUser(CreatePersonRequest createPersonRequest) {
        log.info("Creating person with name {}", createPersonRequest.getName());
        log.info("userId: {}", createPersonRequest.getUserId());
        var details = integrationRepository.findByUserIdAndIntegrationType(createPersonRequest.getUserId(), IntegrationType.PIPEDRIVE);
        var refreshToken = details.getRefreshToken();
        var accessToken = pipeDriveAuthService.refreshAccessToken(refreshToken);
        return pipedriveCreatePersonService.createPerson(createPersonRequest,accessToken.getAccessToken());
    }

    @Transactional
    public PersonFieldsResponse getContactsByUserId(String userId) {
        log.info("getContacts");
        log.info("userId: {}", userId);
        var details = integrationRepository.findByUserIdAndIntegrationType(userId, IntegrationType.PIPEDRIVE);
        var refreshToken = details.getRefreshToken();
        var accessToken = pipeDriveAuthService.refreshAccessToken(refreshToken);
        return pipedrivePersonFieldService.getPersonFields(accessToken.getAccessToken());
    }

    @Transactional
    public PersonsResponse getDetailsByUserId(String userId) {
        log.info("Getting Person Details");
        log.info("userId: {}", userId);
        var details = integrationRepository.findByUserIdAndIntegrationType(userId, IntegrationType.PIPEDRIVE);
        var refreshToken = details.getRefreshToken();
        var accessToken = pipeDriveAuthService.refreshAccessToken(refreshToken);
        return pipedrivePersonService.getPersons(accessToken.getAccessToken());
    }

}
