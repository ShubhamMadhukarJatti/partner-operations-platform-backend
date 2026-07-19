package com.sharkdom.partnertraining.service;

import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.emailOutreach.repository.EmailRepository;
import com.sharkdom.entity.user.User;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnertraining.dto.*;
import com.sharkdom.partnertraining.entity.*;
import com.sharkdom.partnertraining.enums.UserCourseStatus;
import com.sharkdom.partnertraining.repository.*;
import com.sharkdom.repository.user.UserRepository;
import com.sharkdom.service.email.EmailService;
import com.sharkdom.util.Util;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class UserCourseStatusService {

    @Autowired private UserCourseStatusRepository userCourseStatusRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private CourseStageRepository courseStageRepository;
    @Autowired private MyPartnerUserCourseStatusRepository myPartnerUserCourseStatusRepository;
    @Autowired private CourseCertificateRepository courseCertificateRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailRepository emailRepository;
    @Autowired private EmailService emailService;

    @Value("${env}") private String env;
    private static final String ALGO="AES";

    // ================= SAVE STATUS =================
    public UserCourseStatusResponse saveStatus(UserCourseStatusRequest req){
        log.info("Save status | userId={} | courseId={} | status={}",req.getUserId(),req.getCourseId(),req.getStatus());
        UserCourseStatusEntity e=userCourseStatusRepository.findByUserIdAndCourseId(req.getUserId(),req.getCourseId())
                .orElseGet(()->{ UserCourseStatusEntity x=new UserCourseStatusEntity(); x.setUserId(req.getUserId()); x.setCourseId(req.getCourseId()); return x; });
        e.setStatus(req.getStatus()); userCourseStatusRepository.save(e);
        return UserCourseStatusResponse.builder().userId(req.getUserId()).courseId(req.getCourseId()).status(e.getStatus()).build();
    }

    // ================= UPDATE STATUS =================
    @Transactional
    public UserCourseStatusResponse updateStatus(UpdateUserCourseStatusRequest req){
        log.info("Update status | userId={} | courseId={} | status={}",req.getUserId(),req.getCourseId(),req.getStatus());

        UserCourseStatusEntity e=userCourseStatusRepository.findByUserIdAndCourseId(req.getUserId(),req.getCourseId())
                .orElseThrow(()->new IllegalStateException("Course status not found"));

        UserCourseStatus old=e.getStatus(), now=req.getStatus();
        e.setStatus(now); userCourseStatusRepository.save(e);

        // First time COMPLETED trigger
        if(UserCourseStatus.COMPLETED.equals(now)&&!UserCourseStatus.COMPLETED.equals(old)){
            Course course=courseRepository.findById(req.getCourseId()).orElseThrow(()->new RuntimeException("Course not found"));
            String certUrl=course.getCertificateUrl();

            if(certUrl!=null && !certUrl.trim().isEmpty()){
                courseCertificateRepository.save(CourseCertificate.builder().courseId(req.getCourseId()).userId(req.getUserId()).certificateUrl(certUrl).build());

                String utm=encrypt(e.getUserId()+":"+req.getCourseId());
                String base=env.equalsIgnoreCase("dev")?"https://dev.sharkdom.com/partner-course/utm=":"https://sharkdom.com/partner-course/utm=";
                String finalUrl=base+utm;

                User user=userRepository.findByUserId(req.getUserId()).orElseThrow(()->new RuntimeException("User not found"));
                emailService.sendByEmailAddTeamMember("download_certificate",user.getEmail(),finalUrl,user.getName());

                log.info("Certificate sent | userId={} | courseId={}",req.getUserId(),req.getCourseId());
            }else log.warn("Certificate URL missing | courseId={}",req.getCourseId());
        }

        return UserCourseStatusResponse.builder().userId(req.getUserId()).courseId(e.getCourseId()).status(e.getStatus()).build();
    }

    // ================= ENCRYPT =================
    public String encrypt(String data){
        try{
            String key=env.equalsIgnoreCase("dev")?"Uz1FyvLoNnIKdGjMIRPDKccr":"KzaFdvfoDOIFd9SMIQPDKcE1";
            SecretKeySpec sk=new SecretKeySpec(key.getBytes(),ALGO);
            Cipher c=Cipher.getInstance(ALGO); c.init(Cipher.ENCRYPT_MODE,sk);
            return Base64.getEncoder().encodeToString(c.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        }catch(Exception e){ throw new ServiceException(ErrorMessages.SH116,e.getMessage()); }
    }

    // ================= DASHBOARD =================
    public CourseDashboardResponse getDashboardCourses(String userId,UserCourseStatus status,int page,int size){
        List<CourseCardDto> cont=userCourseStatusRepository.findAllByUserIdAndStatus(userId,UserCourseStatus.IN_PROGRESS)
                .stream().map(x->mapToCard(userId,courseRepository.findById(x.getCourseId()).orElseThrow())).toList();

        Pageable pageable=PageRequest.of(page,size); Page<UserCourseStatusEntity> res;

        if(status==null) res=userCourseStatusRepository.findByUserId(userId,pageable);
        else if(status==UserCourseStatus.NOT_STARTED) res=userCourseStatusRepository.findByUserIdAndStatus(userId,UserCourseStatus.ASSIGNED,pageable);
        else res=userCourseStatusRepository.findByUserIdAndStatus(userId,status,pageable);

        List<CourseCardDto> courses=res.getContent().stream()
                .map(x->mapToCard(userId,courseRepository.findById(x.getCourseId()).orElseThrow())).toList();

        return CourseDashboardResponse.builder().continueCourses(cont).courses(courses)
                .totalElements(res.getTotalElements()).totalPages(res.getTotalPages()).currentPage(page).build();
    }

    // ================= MAP =================
    private CourseCardDto mapToCard(String userId,Course c){
        UserCourseStatusEntity s=userCourseStatusRepository.findByUserIdAndCourseId(userId,c.getId()).orElse(null);
        UserCourseStatus st=(s==null)?UserCourseStatus.NOT_STARTED:s.getStatus();

        return CourseCardDto.builder()
                .courseId(c.getId()).title(c.getTitle()).thumbnailUrl(c.getCoverImageUrl())
                .modules(courseStageRepository.countByCourseIdAndActiveTrue(c.getId()))
                .durationInMinutes(c.getDurationMinutes()).level(c.getLevel().name())
                .status(st).progressPercentage(s==null?0:calculateProgress(userId,c)).build();
    }

    private int calculateProgress(String userId,Course c){
        int total=courseStageRepository.countByCourseIdAndActiveTrue(c.getId()), done=0;
        return total==0?0:(done*100)/total;
    }

    // ================= INSIGHTS =================
    public CourseInsightsResponse getCourseInsights(Long courseId){
        long totalP=myPartnerUserCourseStatusRepository.countDistinctAssignedOrgIdByCourseId(courseId);
        long done=myPartnerUserCourseStatusRepository.countByCourseIdAndStatus(courseId,UserCourseStatus.COMPLETED);
        long prog=myPartnerUserCourseStatusRepository.countByCourseIdAndStatus(courseId,UserCourseStatus.IN_PROGRESS);

        double adoption=totalP==0?0:( (done+prog)*100.0)/totalP;
        double completion=totalP==0?0:(done*100.0)/totalP;

        long totalU=userCourseStatusRepository.countByCourseId(courseId);
        long inProg=userCourseStatusRepository.countByCourseIdAndStatus(courseId,UserCourseStatus.IN_PROGRESS);
        double avg=totalU==0?0:(inProg*100.0)/totalU;

        return CourseInsightsResponse.builder().totalPartner(totalP).adoption(adoption).completion(completion).avgReadiness(avg).build();
    }

    // ================= PARTNER DASHBOARD =================
    public PartnerDashboardStatsResponse getPartnerDashboardStats(){
        Long orgId=Util.getOrgIdFromToken();
        long total=myPartnerUserCourseStatusRepository.countByAssignedOrgId(orgId);
        long done=myPartnerUserCourseStatusRepository.countByAssignedOrgIdAndStatus(orgId,UserCourseStatus.COMPLETED);
        long prog=myPartnerUserCourseStatusRepository.countByAssignedOrgIdAndStatus(orgId,UserCourseStatus.IN_PROGRESS);
        int avg=total==0?0:(int)((prog*100.0)/total);

        return PartnerDashboardStatsResponse.builder().assignedCourses(total).completedCourses(done).certificates(done).avgReadinessPercentage(avg).build();
    }

    // ================= ORG STATUS =================
    public CourseStatusResponse getAssignedOrgCourseStatus(Long courseId){
        Long orgId=Util.getOrgIdFromToken();
        return myPartnerUserCourseStatusRepository.findByCourseIdAndAssignedOrgId(courseId,orgId)
                .map(e->CourseStatusResponse.builder().courseId(courseId).status(e.getStatus()).build())
                .orElse(CourseStatusResponse.builder().courseId(courseId).status(UserCourseStatus.NOT_STARTED).build());
    }
}