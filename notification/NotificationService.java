package com.sharkdom.service.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.notification.Notification;
import com.sharkdom.entity.notification.NotificationUnsubscribe;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ResourceNotFoundException;
import com.sharkdom.model.email.TemplateOrganizationEmailReqModel;
import com.sharkdom.model.notification.NotificationBuilder;
import com.sharkdom.model.notification.NotificationRequest;
import com.sharkdom.model.organization.OrganizationSearchResponse;
import com.sharkdom.repository.notification.NotificationRepository;
import com.sharkdom.repository.notification.NotificationUnsubscribeRepository;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.service.user.UserService;
import com.sharkdom.util.Util;
import com.sharkdom.util.firebasefcd.FirebaseNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ObjectMapper objectMapper;
    private final NotificationUnsubscribeRepository notificationUnsubscribeRepository;

    @Autowired
    private UserService userService;
    private final EmailService emailService;
    private final OrganizationRepository organizationRepository;

    public NotificationService(NotificationUnsubscribeRepository notificationUnsubscribeRepository, EmailService emailService, OrganizationRepository organizationRepository) {
        this.notificationUnsubscribeRepository = notificationUnsubscribeRepository;
        this.emailService = emailService;
        this.organizationRepository = organizationRepository;
    }

    public Page<Notification> findAllByUserId(String userId, boolean forMobile, boolean forWeb, int page, int size) {
        return notificationRepository.findByUserIdAndForMobileAndForWebOrderByCreationTimestampDesc(userId, forMobile, forWeb, PageRequest.of(page, size));
    }

    public Page<Notification> findAllByOrganizationId(boolean forMobile, boolean forWeb, int page, int size) {
        Long organizationId = Util.getOrgIdFromToken();
        return notificationRepository.findByOrganizationIdAndForMobileAndForWebOrderByCreationTimestampDesc(organizationId, forMobile, forWeb, PageRequest.of(page, size));
    }

    public Page<Notification> findAllForMobileByUserId(String userId, int page, int size) {
        return notificationRepository.findByUserIdAndForMobileOrderByCreationTimestampDesc(userId, true, PageRequest.of(page, size));
    }

    public Page<Notification> findAllForWebByUserId(String userId, int page, int size) {
        return notificationRepository.findByUserIdAndForWebOrderByCreationTimestampDesc(userId, true, PageRequest.of(page, size));
    }

    @Transactional
    public Notification update(Notification updated) throws Exception {
        findById(updated.getId());
        return notificationRepository.save(updated);
    }

    public void saveAll(List<Notification> notifications) {
        notificationRepository.saveAll(notifications);
    }

    public Notification save(Notification notifications) {
        return notificationRepository.save(notifications);
    }

    @Transactional
    public Notification create(Notification notification) {
        notificationRepository.save(notification);
        if (notification.getTemplateCode() != null) {
            emailService.sendByTemplateAndOrganizationIds(TemplateOrganizationEmailReqModel.builder().templateCode(notification.getTemplateCode()).organizationIds(List.of(notification.getOrganizationId())).build(), null, 1L, 1L);
        }
        FirebaseNotificationService.addMessageInQueue(notification);
        return notification;
    }

    @Transactional
    public Notification patch(Long id, JsonPatch patch) throws Exception {
        Notification notification = findById(id);
        Notification userPatched = applyPatchToUser(patch, notification);
        return update(userPatched);
    }

    public Notification findById(Long id) {
        return notificationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.SH23, id));
    }

    private Notification applyPatchToUser(JsonPatch patch, Notification targetUser) throws JsonPatchException, JsonProcessingException {

        JsonNode patched = patch.apply(objectMapper.convertValue(targetUser, JsonNode.class));
        return objectMapper.treeToValue(patched, Notification.class);
    }

    @Async
    public void sendNotificationAllActiveUsers(Notification notification) {
        List<User> userList = userService.findAllActive();
        if (!userList.isEmpty()) {
            List<Notification> notifications = userList.stream().map(user ->
                    new NotificationBuilder()

                            .setSubject(notification.getSubject())
                            .setBody(notification.getBody())
                            .setForMobile(notification.isForMobile())
                            .setForWeb(notification.isForWeb())
                            .addRecordsInAdditionalDataMap(notification.getAdditionalDataMap())
                            .setUserId(user.getUserId())
                            .setMobileDeviceId(user.getDeviceId())
                            .build()
            ).collect(Collectors.toList());

            saveAll(notifications);
            FirebaseNotificationService.addMessagesInQueue(notifications);
        }
    }

    @Async
    public void sendNotificationAllOrganizations(Notification notification) {
        List<String> sectors = null == notification.getPreferredSectors() ? List.of() : notification.getPreferredSectors();
        List<String> partnershipTypes = null == notification.getPreferredPartnershipTypes() ? List.of() : notification.getPreferredPartnershipTypes();
        List<OrganizationSearchResponse> organizationSearchResponses = organizationRepository.searchOrganizationsForNotification(sectors, sectors.isEmpty(), partnershipTypes, partnershipTypes.isEmpty());
        if (!organizationSearchResponses.isEmpty()) {
            List<Notification> notifications = organizationSearchResponses.stream().map(organization -> {
                        Notification current = Notification.builder()
                                .organizationId(notification.getOrganizationId())
                                .subject(notification.getSubject())
                                .body(notification.getBody())
                                .forMobile(notification.isForMobile())
                                .forWeb(notification.isForWeb())
                                .additionalDataMap(notification.getAdditionalDataMap())
                                .build();
                        if (current.getBody().contains("<java>{{organization.name}}</java>")) {
                            var body = current.getBody().replace("<java>{{organization.name}}</java>", organization.getName());
                            current.setBody(body);
                        }
                        return Notification.builder()
                                .organizationId(organization.getId())
                                .subject(current.getSubject())
                                .body(current.getBody())
                                .forMobile(current.isForMobile())
                                .forWeb(current.isForWeb())
                                .additionalDataMap(current.getAdditionalDataMap())
                                .build();
                    }
            ).collect(Collectors.toList());

            saveAll(notifications);
        }
    }

    public void unsubscribe(NotificationRequest notificationRequest) {
        var unsubscribe = notificationUnsubscribeRepository.findByOrganizationIdAndTypeAndNotificationFor(notificationRequest.organizationId(), notificationRequest.type(), notificationRequest.notificationFor());
        if (unsubscribe.isEmpty()) {
            var notificationUnsubscribe = NotificationUnsubscribe.builder()
                    .organizationId(notificationRequest.organizationId())
                    .type(notificationRequest.type())
                    .notificationFor(notificationRequest.notificationFor())
                    .build();
            notificationUnsubscribeRepository.save(notificationUnsubscribe);
        }
    }

    public void subscribe(NotificationRequest notificationRequest) {
        var unsubscribe = notificationUnsubscribeRepository.findByOrganizationIdAndTypeAndNotificationFor(notificationRequest.organizationId(), notificationRequest.type(), notificationRequest.notificationFor());
        unsubscribe.ifPresent(notificationUnsubscribeRepository::delete);
    }
}
