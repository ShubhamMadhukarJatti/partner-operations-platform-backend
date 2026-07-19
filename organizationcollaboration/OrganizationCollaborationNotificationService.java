package com.sharkdom.service.organizationcollaboration;

import com.sharkdom.config.WebSocketHandler;
import com.sharkdom.constants.organization.OrgUserMappingStatus;
import com.sharkdom.constants.organization.OrgUserRole;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.entity.organizationcollaboration.OrganizationCollaboration;
import com.sharkdom.entity.user.User;
import com.sharkdom.model.organization.OrganizationUserMappingResponse;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.organization.OrganizationUserMappingRepository;
import com.sharkdom.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrganizationCollaborationNotificationService {

    @Autowired
    private OrganizationUserMappingRepository organizationUserMappingRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    NotificationService notificationService;

    private final WebSocketHandler webSocketHandler;

    public OrganizationCollaborationNotificationService(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Async
    void sendNewCollabRequestNotification(OrganizationCollaboration organizationCollaboration) {

        Organization organization = organizationRepository.findById(organizationCollaboration.getSenderOrganizationId()).get();
        Notification notification = Notification.builder()
                .subject("New Partnership request!")
                .body("You have received a new partnership request from " + organization.getName() + ".")
                .forMobile(false)
                .forWeb(true)
                .organizationId(organizationCollaboration.getReceiverOrganizationId())
                .build();

        var savedNotification = notificationService.save(notification);
        webSocketHandler.sendMessageToUser(organizationCollaboration.getReceiverOrganizationId(), savedNotification);
        sendCollabRequestNotificationToSender(organizationCollaboration);
    }

    private void sendCollabRequestNotificationToSender(OrganizationCollaboration organizationCollaboration) {
        Organization organization = organizationRepository.findById(organizationCollaboration.getReceiverOrganizationId()).get();
        Notification notification = Notification.builder()
                .subject("New Partnership request!")
                .body("You have sent a new partnership request to " + organization.getName() + ".")
                .forMobile(false)
                .forWeb(true)
                .organizationId(organizationCollaboration.getSenderOrganizationId())
                .build();

        var savedNotification = notificationService.save(notification);
        webSocketHandler.sendMessageToUser(organizationCollaboration.getSenderOrganizationId(), savedNotification);
    }

    @Async
    public void sendCollabRequestAcceptedNotification(OrganizationCollaboration organizationCollaboration) {
        Organization organization = organizationRepository.findById(organizationCollaboration.getReceiverOrganizationId()).get();
        Notification notification = Notification.builder()
                .subject("Partnership request accepted!")
                .body("Your partnership request has been accepted by " + organization.getName() + ".")
                .forMobile(false)
                .forWeb(true)
                .organizationId(organizationCollaboration.getSenderOrganizationId())
                .build();

        var savedNotification = notificationService.save(notification);
        webSocketHandler.sendMessageToUser(organizationCollaboration.getSenderOrganizationId(), savedNotification);
        sendCollabRequestAcceptedNotificationToReceiver(organizationCollaboration);
    }

    public void sendCollabRequestAcceptedNotificationToReceiver(OrganizationCollaboration organizationCollaboration) {
        Organization organization = organizationRepository.findById(organizationCollaboration.getSenderOrganizationId()).get();
        Notification notification = Notification.builder()
                .subject("Partnership request accepted!")
                .body("You have accepted partnership request from " + organization.getName() + ".")
                .forMobile(false)
                .forWeb(true)
                .organizationId(organizationCollaboration.getReceiverOrganizationId())
                .build();

        var savedNotification = notificationService.save(notification);
        webSocketHandler.sendMessageToUser(organizationCollaboration.getReceiverOrganizationId(), savedNotification);

    }

    private List<User> getAllActiveAdminsOfOrganization(long organizationId) {
        List<OrganizationUserMappingResponse> organizationUserMappingsList = organizationUserMappingRepository
                .findAllByOrganizationIdAndRoleAndStatus(organizationId, OrgUserRole.ADMIN, OrgUserMappingStatus.ACTIVE);

        return organizationUserMappingsList.stream().
                filter(collaboration -> collaboration.getUser().getStatus().equals("ACTIVE")).map(organization -> organization.getUser()).
                collect(Collectors.toList());
    }

}
