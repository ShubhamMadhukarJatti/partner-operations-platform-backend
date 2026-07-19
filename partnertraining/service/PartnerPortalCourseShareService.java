package com.sharkdom.partnertraining.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnertraining.dto.*;
import com.sharkdom.partnertraining.entity.*;
import com.sharkdom.partnertraining.enums.UserCourseStatus;
import com.sharkdom.partnertraining.repository.*;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartnerPortalCourseShareService {

    private final PartnerPortalCourseShareRepository repository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final OrganizationRepository organizationRepository;
    private final UserCourseStatusRepository userCourseStatusRepository;

    @Value("${env}") private String env;
    private static final String ALGO="AES";

    // ========================= SHARE COURSE =========================
    public CourseShareResponse shareCourse(CourseShareRequest req){
        Long orgId=Util.getOrgIdFromToken(); String email=req.getReceiverUserEmail();
        log.info("Share course | courseId={} | orgId={} | email={}",req.getCourseId(),orgId,email);

        // Resolve/Create user
        Optional<User> userOpt=userRepository.findByEmail(email);
        boolean isNew=userOpt.isEmpty();
        String userId=isNew?UUID.randomUUID().toString():userOpt.get().getUserId();

        if(isNew){
            userRepository.save(User.builder().email(email).userId(userId).build());
            log.info("New user created | email={} | userId={}",email,userId);
        }

        // UPSERT share
        PartnerPortalCourseShare share=repository
                .findByCourseIdAndSenderOrganizationIdAndReceiverUserId(req.getCourseId(),orgId,userId)
                .orElseGet(PartnerPortalCourseShare::new);

        share.setCourseId(req.getCourseId()); share.setSenderOrganizationId(orgId);
        share.setReceiverUserId(userId); share.setReceiverUserEmail(email); share.setActive(true);

        // Generate token URL
        String payload=userId+":"+orgId+":"+req.getCourseId()+":"+email;
        String utm=encrypt(payload);
        String base=env.equalsIgnoreCase("dev")?"https://dev.sharkdom.com/partner-course/login?utm=":"https://sharkdom.com/partner-course/login?utm=";
        String url=base+utm;

        share.setSharedUrl(url);
        PartnerPortalCourseShare saved=repository.save(share);

        log.info("Course shared | id={} | url={}",saved.getId(),url);

        // Send email
        emailService.sendByEmailAddTeamMember("partner_snapshot_invite",email,url,"Sharkdom");

        // Assign course status
        UserCourseStatusEntity status=new UserCourseStatusEntity();
        status.setCourseId(req.getCourseId()); status.setStatus(UserCourseStatus.ASSIGNED);
        status.setUserId(userId); status.setAssigningOrgId(orgId);
        userCourseStatusRepository.save(status);

        return CourseShareResponse.builder()
                .id(saved.getId()).courseId(saved.getCourseId())
                .receiverUserEmail(saved.getReceiverUserEmail())
                .sharedUrl(saved.getSharedUrl()).active(saved.isActive())
                .build();
    }

    // ========================= ENCRYPT =========================
    public String encrypt(String data){
        try{
            String key=env.equalsIgnoreCase("dev")?"Uz1FyvLoNnIKdGjMIRPDKccr":"KzaFdvfoDOIFd9SMIQPDKcE1";
            SecretKeySpec sk=new SecretKeySpec(key.getBytes(),ALGO);
            Cipher c=Cipher.getInstance(ALGO); c.init(Cipher.ENCRYPT_MODE,sk);
            return Base64.getEncoder().encodeToString(c.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        }catch(Exception e){ throw new ServiceException(ErrorMessages.SH116,e.getMessage()); }
    }
}