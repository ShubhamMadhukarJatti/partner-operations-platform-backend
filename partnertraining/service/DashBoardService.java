package com.sharkdom.partnertraining.service;

import com.sharkdom.model.organizatiocollaboration.CollaborationStatus;
import com.sharkdom.partnertraining.dto.*;
import com.sharkdom.partnertraining.entity.*;
import com.sharkdom.partnertraining.enums.UserCourseStatus;
import com.sharkdom.partnertraining.repository.*;
import com.sharkdom.repository.organization.OrganizationRepository;
import com.sharkdom.service.organizationcollaboration.OrganizationCollaborationService;
import com.sharkdom.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashBoardService {

    private final CourseRepository courseRepository;
    private final OrganizationCollaborationService organizationCollaborationService;
    private final PartnerPortalCourseShareRepository partnerPortalCourseShareRepository;
    private final CourseStageRepository courseStageRepository;
    private final UserCourseStatusRepository userCourseStatusRepository;
    private final MyPartnerUserCourseStatusRepository myPartnerUserCourseStatusRepository;
    private final OrganizationRepository organizationRepository;

    // ========================= DASHBOARD OVERVIEW =========================
    public DashboardResponse getDashboardOverview(){
        var totalPartner=organizationCollaborationService.getAllCollaborationsCount(CollaborationStatus.ACTIVE);
        return DashboardResponse.builder()
                .totalCourses(courseRepository.countByCreatedBy_IdAndPublishedTrue(Util.getOrgIdFromToken()))
                .totalPartners(totalPartner)
                .activePartners(partnerPortalCourseShareRepository.countBySenderOrganizationId(Util.getOrgIdFromToken()))
                .avgReadinessPercentage(getAvgCompletedCoursesBySenderOrg()+getAvgCompletedCoursesBySenderOrgForMyPartner())
                .partnerReadiness(getPartnerReadiness())
                .coursePerformance(getMergedCoursePerformance())
                .build();
    }

    // ========================= AVG COMPLETED COURSES =========================
    public double getAvgCompletedCoursesBySenderOrg(){
        Long orgId=Util.getOrgIdFromToken();
        List<PartnerPortalCourseShare> shares=partnerPortalCourseShareRepository.findAllBySenderOrganizationIdAndActiveTrue(orgId);
        if(shares.isEmpty()) return 0.0;
        Set<String> users=shares.stream().map(PartnerPortalCourseShare::getReceiverUserId).collect(Collectors.toSet());
        if(users.isEmpty()) return 0.0;
        long completed=0;
        for(String u:users) completed+=userCourseStatusRepository.countByUserIdAndStatus(u,UserCourseStatus.COMPLETED);
        return (double)completed/users.size();
    }

    // ========================= COURSE PERFORMANCE =========================
    public List<CoursePerformanceResponse> getCoursePerformance(){
        Long orgId=Util.getOrgIdFromToken();
        List<Course> courses=courseRepository.findAllByCreatedBy_IdAndPublishedTrue(orgId);
        List<CoursePerformanceResponse> res=new ArrayList<>();
        for(Course c:courses){
            long enrolled=userCourseStatusRepository.countByCourseId(c.getId());
            long completed=userCourseStatusRepository.countByCourseIdAndStatus(c.getId(),UserCourseStatus.COMPLETED);
            double avg=enrolled==0?0.0:(completed*100.0)/enrolled;
            res.add(new CoursePerformanceResponse(c.getTitle(),enrolled,completed,avg));
        }
        return res;
    }

    // ========================= MY PARTNER AVG =========================
    public double getAvgCompletedCoursesBySenderOrgForMyPartner(){
        Long orgId=Util.getOrgIdFromToken();
        long total=myPartnerUserCourseStatusRepository.countByAssigningOrgId(orgId);
        if(total==0) return 0.0;
        long completed=myPartnerUserCourseStatusRepository.countByAssigningOrgIdAndStatus(orgId,UserCourseStatus.COMPLETED);
        return (completed*100.0)/total;
    }

    // ========================= MY PARTNER COURSE PERFORMANCE =========================
    public List<CoursePerformanceResponse> getCoursePerformanceForMyPartner(){
        Long orgId=Util.getOrgIdFromToken();
        List<Course> courses=courseRepository.findAllByCreatedBy_IdAndPublishedTrue(orgId);
        List<CoursePerformanceResponse> res=new ArrayList<>();
        for(Course c:courses){
            long enrolled=myPartnerUserCourseStatusRepository.countByAssigningOrgIdAndCourseId(orgId,c.getId());
            long completed=myPartnerUserCourseStatusRepository.countByAssigningOrgIdAndCourseIdAndStatus(orgId,c.getId(),UserCourseStatus.COMPLETED);
            double avg=enrolled==0?0.0:(completed*100.0)/enrolled;
            res.add(new CoursePerformanceResponse(c.getTitle(),enrolled,completed,avg));
        }
        return res;
    }

    // ========================= MERGED COURSE PERFORMANCE =========================
    public List<CoursePerformanceResponse> getMergedCoursePerformance(){
        Map<String,CoursePerformanceResponse> map=new HashMap<>();
        for(CoursePerformanceResponse r:getCoursePerformance()) map.put(r.getCourseTitle(),new CoursePerformanceResponse(r.getCourseTitle(),r.getEnrolled(),r.getCompleted(),r.getAvgCompletion()));
        for(CoursePerformanceResponse r:getCoursePerformanceForMyPartner()){
            var ex=map.get(r.getCourseTitle());
            if(ex==null) map.put(r.getCourseTitle(),new CoursePerformanceResponse(r.getCourseTitle(),r.getEnrolled(),r.getCompleted(),r.getAvgCompletion()));
            else{
                long enrolled=ex.getEnrolled()+r.getEnrolled();
                long completed=ex.getCompleted()+r.getCompleted();
                double avg=enrolled==0?0.0:(completed*100.0)/enrolled;
                ex.setEnrolled(enrolled); ex.setCompleted(completed); ex.setAvgCompletion(avg);
            }
        }
        return new ArrayList<>(map.values());
    }

    // ========================= INTERNAL AGGREGATION =========================
    private static class PartnerAggregation{ String partnerName; long noOfUsers; Set<Long> courseIds=new HashSet<>(); long completedCourses; }

    private String resolvePartnerName(Long orgId){
        if(orgId==null) return "Unknown Partner";
        return organizationRepository.findById(orgId).map(o->o.getName()).orElse("Unknown Partner");
    }

    // ========================= PARTNER READINESS =========================
    public List<PartnerReadinessDto> getPartnerReadiness(){
        Long orgId=Util.getOrgIdFromToken();
        Map<String,PartnerAggregation> map=new HashMap<>();

        // MY PARTNER DATA
        List<MyPartnerUserCourseStatusEntity> pData=myPartnerUserCourseStatusRepository.findAllByAssigningOrgId(orgId);
        for(MyPartnerUserCourseStatusEntity e:pData){
            String key=e.getAssignedOrgId()!=null?String.valueOf(e.getAssignedOrgId()):"UNKNOWN";
            PartnerAggregation agg=map.computeIfAbsent(key,k->new PartnerAggregation());
            agg.partnerName=resolvePartnerName(e.getAssignedOrgId()); agg.noOfUsers=1; agg.courseIds.add(e.getCourseId());
            if(e.getStatus()==UserCourseStatus.COMPLETED) agg.completedCourses++;
        }

        // USER COURSE DATA
        List<UserCourseStatusEntity> uData=userCourseStatusRepository.findAllByAssigningOrgId(orgId);
        for(UserCourseStatusEntity e:uData){
            String key=e.getUserId();
            PartnerAggregation agg=map.computeIfAbsent(key,k->new PartnerAggregation());
            String email=partnerPortalCourseShareRepository.findByReceiverUserId(e.getUserId()).get().getReceiverUserEmail();
            agg.partnerName=getNameFromEmailDomain(email); agg.noOfUsers=1; agg.courseIds.add(e.getCourseId());
            if(e.getStatus()==UserCourseStatus.COMPLETED) agg.completedCourses++;
        }

        List<PartnerReadinessDto> res=new ArrayList<>();
        for(PartnerAggregation agg:map.values()){
            long enrolled=agg.courseIds.size();
            double readiness=enrolled==0?0.0:(agg.completedCourses*100.0)/enrolled;
            res.add(new PartnerReadinessDto(agg.partnerName,agg.noOfUsers,enrolled,readiness));
        }
        return res;
    }

    // ========================= ASSOCIATED PARTNERS =========================
    public List<String> getAssociatedPartnerNames(){
        Long orgId=Util.getOrgIdFromToken();
        Set<String> names=new LinkedHashSet<>();

        List<MyPartnerUserCourseStatusEntity> myData=myPartnerUserCourseStatusRepository.findAllByAssigningOrgId(orgId);
        for(MyPartnerUserCourseStatusEntity e:myData)
            if(e.getAssignedOrgId()!=null)
                organizationRepository.findById(e.getAssignedOrgId()).map(o->o.getName()).ifPresent(names::add);

        List<UserCourseStatusEntity> userData=userCourseStatusRepository.findAllByAssigningOrgId(orgId);
        for(UserCourseStatusEntity e:userData){
            if(e.getUserId()!=null){
                String name=partnerPortalCourseShareRepository.findByReceiverUserId(e.getUserId()).map(PartnerPortalCourseShare::getReceiverUserEmail).map(this::getNameFromEmailDomain).orElse("Unknown");
                names.add(name);
            }
        }
        return new ArrayList<>(names);
    }

    // ========================= MY PARTNER ORG NAMES =========================
    public List<String> getMyPartnerOrganizationNames(){
        Long orgId=Util.getOrgIdFromToken();
        return myPartnerUserCourseStatusRepository.findAllByAssigningOrgId(orgId).stream()
                .map(MyPartnerUserCourseStatusEntity::getAssignedOrgId).filter(Objects::nonNull).distinct()
                .map(id->organizationRepository.findById(id).map(o->o.getName()).orElse(null))
                .filter(Objects::nonNull).toList();
    }

    // ========================= PARTNER PORTAL STATS =========================
    public DashboardStatsResponse getDashboardStatsForPartnerPortal(String userId){
        long assigned=userCourseStatusRepository.findAllByUserId(userId).size();
        long completed=userCourseStatusRepository.countByUserIdAndStatus(userId,UserCourseStatus.COMPLETED);
        long inProgress=userCourseStatusRepository.countByUserIdAndStatus(userId,UserCourseStatus.IN_PROGRESS);
        int avg=assigned>0?(int)(((completed+inProgress)*100)/assigned):0;

        return DashboardStatsResponse.builder()
                .assignedCourses(assigned)
                .completedCourses(completed)
                .certificates(completed)
                .avgReadinessScore(avg)
                .build();
    }

    // ========================= PARTNER NAMES BY COURSE =========================
    public List<String> getAssociatedPartnerNamesWithCourseId(Long courseId){
        return myPartnerUserCourseStatusRepository.findAllByCourseId(courseId).stream()
                .map(MyPartnerUserCourseStatusEntity::getAssignedOrgId).distinct()
                .map(organizationRepository::findNameById).filter(Objects::nonNull).distinct().toList();
    }

    // ========================= EMAIL DOMAIN TO NAME =========================
    public String getNameFromEmailDomain(String email){
        if(email==null||!email.contains("@")) return "Unknown";
        String domain=email.substring(email.indexOf("@")+1).split("\\.")[0];
        return formatDomainName(domain);
    }

    private String formatDomainName(String domain){
        if(domain==null||domain.isBlank()) return "Unknown";
        String[] words=domain.split("[-_]");
        StringBuilder name=new StringBuilder();
        for(String w:words) name.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        return name.toString().trim();
    }
}