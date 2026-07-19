package com.sharkdom.repository.organization;


import com.sharkdom.constants.organization.OrganizationStatus;
import com.sharkdom.constants.organization.Source;
import com.sharkdom.dto.OrganizationUpdateView;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.model.organization.OrgData;
import com.sharkdom.model.organization.OrgEmailHistoryResponse;
import com.sharkdom.model.organization.OrgSectorResponse;
import com.sharkdom.model.organization.OrganizationSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByCode(String code);

    boolean existsOrganizationByCode(String code);

    boolean existsByWebsiteIgnoreCase(String website);

    boolean existsOrganizationByName(String name);

    boolean existsByPrimaryEmail(String email);

    Page<Organization> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Organization> findAllByPrimaryEmailContainingIgnoreCase(String name, Pageable pageable);

    Page<Organization>
    findAllByNameContainingIgnoreCaseOrPrimaryEmailContainingIgnoreCase(
            String name,
            String email,
            Pageable pageable
    );


    @Query(value = "SELECT DISTINCT\n" +
            "    org.id as id,  \n" +
            "    org.code as code, \n" +
            "    org.name as name, \n" +
            "    org.about as about, \n" +
            "    org.briefDescription as briefDescription, \n" +
            "    org.sector as sector, \n" +
            "    org.stage as stage, \n" +
            "    org.city as city, \n" +
            "    org.state as state, \n" +
            "    org.verified as verified, \n" +
            "    org.targetMarket as targetMarket, \n" +
            "    org.rating as rating, \n" +
            "    org.logoUrl as logoUrl, \n" +
            "    COALESCE(org.inceptionYear, 0) as inceptionYear, \n" +
            "    org.lastUpdatedTimestamp as lastUpdatedTimestamp \n" +
            "FROM \n" +
            "    Organization org\n" +
            "LEFT JOIN \n" +
            "    org.preferredSectors preferredSectors\n" +
            "LEFT JOIN \n" +
            "    org.preferredPartnershipTypes preferredPartnershipTypes\n" +
            "LEFT JOIN \n" +
            "    org.preferredSubSectors preferredSubSectors\n" +
            "WHERE \n" +
            "    (:city = '' OR upper(org.city) = upper(:city)) \n" +
            "    AND (:state = '' OR upper(org.state) = upper(:state))\n" +
            "    AND (:ignoreStage = true OR upper(org.stage) in :stages)\n" +
            "   and (:ignoreSector = true OR upper(preferredSectors.area) in :sectors)\n" +
            "   and (:ignoreSubSectors = true OR upper(preferredSubSectors.area) in :subSectors)\n" +
            "   and (:ignorePartnershipTypes = true OR upper(preferredPartnershipTypes.area) in :partnershipTypes)\n" +
            "    AND (:inceptionYearFrom = 0 OR COALESCE(org.inceptionYear, 0) >= :inceptionYearFrom ) \n" +
            "    AND (:includeUnverified = true or org.verified = true)\n" +
            "    AND org.id <> :queryingOrganizationId \n" +
            "    AND org.status = :status \n" +
            "ORDER BY \n" +
            "    org.lastUpdatedTimestamp DESC\n")
    Page<OrganizationSearchResponse> searchOrganization(String city, String state, List<String> stages, boolean ignoreStage, int inceptionYearFrom,
                                                        List<String> sectors, boolean ignoreSector, boolean includeUnverified, long queryingOrganizationId, OrganizationStatus status, List<String> partnershipTypes, boolean ignorePartnershipTypes, List<String> subSectors, boolean ignoreSubSectors, Pageable pageable);

    List<Organization> findAllByIdIn(List<Long> organizationIdList);

    @Query("SELECT id FROM Organization where verified=false")
    List<Long> findIdsByUnverified();

    @Query(value = "SELECT DISTINCT \n" +
            "    org.id as id,  \n" +
            "    org.code as code, \n" +
            "    org.name as name, \n" +
            "    org.about as about, \n" +
            "    org.briefDescription as briefDescription, \n" +
            "    org.sector as sector, \n" +
            "    org.stage as stage, \n" +
            "    org.city as city, \n" +
            "    org.state as state, \n" +
            "    org.verified as verified, \n" +
            "    org.targetMarket as targetMarket, \n" +
            "    org.rating as rating, \n" +
            "    org.logoUrl as logoUrl, \n" +
            "    org.sectorType as sectorType, \n" +
            "    COALESCE(org.inceptionYear, 0) as inceptionYear, \n" +
            "    org.lastUpdatedTimestamp as lastUpdatedTimestamp \n" +
            "FROM \n" +
            "    Organization org\n" +
            "LEFT JOIN \n" +
            "    org.preferredSectors preferredSectors\n" +
            "LEFT JOIN \n" +
            "    org.preferredPartnershipTypes preferredPartnershipTypes\n" +
            "WHERE \n" +
            "  org.id not in :queryingOrganizationId \n" +
            "    AND org.status = :status \n" +
            "ORDER BY \n" +
            "    org.lastUpdatedTimestamp DESC\n")
    Page<OrganizationSearchResponse> searchRandomOrganization(Set<Long> queryingOrganizationId, OrganizationStatus status, Pageable pageable);

    @Query("SELECT id FROM Organization where about is null")
    List<Long> findIdsByAboutNull();

    @Query("SELECT o FROM Organization o where o.status=0 and o.emailUnsubscribed = false")
    List<Organization> getAllOrganizationsStatusActiveAndEmailUnsubscribed();

    @Query("SELECT o FROM Organization o where o.status=0 and o.emailUnsubscribed = false and o.lastUpdatedTimestamp >= :updateAfter AND o.lastUpdatedTimestamp < :updateBefore")
    List<Organization> getAllOrganizationsStatusActiveAndEmailUnsubscribedBetweenDates(@Param("updateAfter") Date updateAfter, @Param("updateBefore") Date updateBefore);

    @Query("SELECT e.name FROM Organization e WHERE e.id = :id")
    String findNameById(Long id);

    @Query("SELECT e.status FROM Organization e WHERE e.id = :id")
    OrganizationStatus findStatusById(Long id);

    @Query("SELECT e.logoUrl FROM Organization e WHERE e.id = :id")
    String findLogoUrlById(Long id);

    @Query("SELECT o.primaryEmail From Organization o WHERE o.id IN (:ids)")
    List<String> findEmailsByIdIn(List<Long> ids);

    @Query("SELECT o.primaryEmail From Organization o WHERE o.id  = :id")
    String findEmailById(Long id);

    Optional<Organization> findByPrimaryEmail(String email);

    @Query(value = "SELECT DISTINCT\n" +
            "    org.id as id,  \n" +
            "    org.code as code, \n" +
            "    org.name as name, \n" +
            "    org.about as about, \n" +
            "    org.briefDescription as briefDescription, \n" +
            "    org.sector as sector, \n" +
            "    org.stage as stage, \n" +
            "    org.city as city, \n" +
            "    org.state as state, \n" +
            "    org.verified as verified, \n" +
            "    org.targetMarket as targetMarket, \n" +
            "    org.rating as rating, \n" +
            "    org.logoUrl as logoUrl, \n" +
            "    org.sectorType as sectorType, \n" +
            "    COALESCE(org.inceptionYear, 0) as inceptionYear, \n" +
            "    org.lastUpdatedTimestamp as lastUpdatedTimestamp, \n" +
            "    org.companyType as companyType, \n" +
            "    org.acknowledgmentTime as acknowledgmentTime, \n"+
            "    org.activePartnerships as activePartnerships ,\n"+
            "    org.pipelinePartnerships as pipelinePartnerships \n"+
            "FROM \n" +
            "    Organization org\n" +
            "LEFT JOIN \n" +
            "    org.preferredSectors preferredSectors\n" +
            "LEFT JOIN \n" +
            "    org.preferredPartnershipTypes preferredPartnershipTypes\n" +
            "LEFT JOIN \n" +
            "    org.preferredSubSectors preferredSubSectors\n" +
            "WHERE \n" +
            "    (:city = '' OR upper(org.city) = upper(:city)) \n" +
            "    AND (:state = '' OR upper(org.state) = upper(:state))\n" +
            "    AND (:ignoreStage = true OR upper(org.stage) in :stages)\n" +
            "   and (:ignoreSector = true OR upper(preferredSectors.area) in :sectors)\n" +
            "   and (:ignoreSubSectors = true OR upper(preferredSubSectors.area) in :subSectors)\n" +
            "   and (:ignorePartnershipTypes = true OR upper(preferredPartnershipTypes.area) in :partnershipTypes)\n" +
            "    AND (:ignoreCompanyType = true or org.companyType in :companyType)\n" +
            "    AND (:inceptionYearFrom = 0 OR COALESCE(org.inceptionYear, 0) >= :inceptionYearFrom ) \n" +
            "    AND (:includeUnverified = true or org.verified = true)\n" +
            "    AND org.id NOT IN :queryingOrganizationId \n" +
            "    AND org.name like CONCAT('%', :partialName, '%') \n" +
            "    AND org.status = :status \n" +
            "ORDER BY \n" +
            "    org.lastUpdatedTimestamp DESC\n")
    List<OrganizationSearchResponse> searchPartialOrganization(String city, String state, List<String> stages, boolean ignoreStage, int inceptionYearFrom,
                                                               List<String> sectors, boolean ignoreSector, boolean includeUnverified, List<Long> queryingOrganizationId, OrganizationStatus status, List<String> partnershipTypes, boolean ignorePartnershipTypes, String partialName, List<String> subSectors, boolean ignoreSubSectors,
                                                               List<String> companyType, boolean ignoreCompanyType);

    @Query("SELECT id as id, name as name, sector as sector, primaryEmail as primaryEmail FROM Organization where emailUnsubscribed = false and status = 0")
    List<OrgSectorResponse> findIds();

    @Query(value = "SELECT DISTINCT\n" +
            "    org.id as id, \n" +
            "    org.name as name, \n" +
            "    org.logoUrl as logoUrl, \n" +
            "    org.briefDescription as description, \n" +
            "    org.lastUpdatedTimestamp as lastUpdatedTimestamp \n" +
            "FROM \n" +
            "    Organization org\n" +
            "WHERE \n" +
            "    org.id not in (:organizationIds) \n" +
            "    AND org.status = 0 \n" +
            "ORDER BY \n" +
            "    org.lastUpdatedTimestamp DESC\n" +
            "  LIMIT 1\n")
    OrgEmailHistoryResponse getOrgNameAndDescription(List<Long> organizationIds);

    @Query(value = "SELECT DISTINCT\n" +
            "    org.id as id, \n" +
            "    org.name as name, \n" +
            "    org.logoUrl as logoUrl, \n" +
            "    org.briefDescription as description, \n" +
            "    org.lastUpdatedTimestamp as lastUpdatedTimestamp \n" +
            "FROM \n" +
            "    Organization org\n" +
            "LEFT JOIN \n" +
            "    org.preferredSectors preferredSectors\n" +
            "WHERE \n" +
            "    org.id not in (:organizationIds) \n" +
            "    AND org.status = 0 \n" +
            "   and  upper(preferredSectors.area) = :sector\n" +
            "ORDER BY \n" +
            "    org.lastUpdatedTimestamp DESC\n" +
            "  LIMIT 1\n")
    OrgEmailHistoryResponse getOrgNameAndDescriptionBySector(List<Long> organizationIds, String sector);

    boolean existsOrganizationById(Long organizationId);

    @Query(value = "SELECT \n" +
            "    DATE(creationTimestamp) AS date,\n" +
            "    name AS name\n" +
            "FROM \n" +
            "   Organization \n" +
            "WHERE \n" +
            "   DATE(creationTimestamp) BETWEEN :from AND :to\n")
    List<OrgData> findAllFromTo(LocalDate from, LocalDate to);

    @Query(value = "SELECT DISTINCT\n" +
            "    org.id as id,  \n" +
            "    org.code as code, \n" +
            "    org.name as name, \n" +
            "    org.about as about, \n" +
            "    org.briefDescription as briefDescription, \n" +
            "    org.sector as sector, \n" +
            "    org.stage as stage, \n" +
            "    org.city as city, \n" +
            "    org.state as state, \n" +
            "    org.verified as verified, \n" +
            "    org.targetMarket as targetMarket, \n" +
            "    COALESCE(org.inceptionYear, 0) as inceptionYear, \n" +
            "    org.lastUpdatedTimestamp as lastUpdatedTimestamp \n" +
            "FROM \n" +
            "    Organization org\n" +
            "LEFT JOIN \n" +
            "    org.preferredSectors preferredSectors\n" +
            "LEFT JOIN \n" +
            "    org.preferredPartnershipTypes preferredPartnershipTypes\n" +
            "WHERE \n" +
            "  (:ignoreSector = true OR upper(preferredSectors.area) in :sectors)\n" +
            "   and (:ignorePartnershipTypes = true OR upper(preferredPartnershipTypes.area) in :partnershipTypes)\n" +
            "ORDER BY \n" +
            "    org.lastUpdatedTimestamp DESC\n")
    List<OrganizationSearchResponse> searchOrganizationsForNotification(List<String> sectors, boolean ignoreSector, List<String> partnershipTypes, boolean ignorePartnershipTypes);

    @Query(value = "SELECT DISTINCT \n" +
            "    org.id as id,  \n" +
            "    org.code as code, \n" +
            "    org.name as name, \n" +
            "    org.about as about, \n" +
            "    org.briefDescription as briefDescription, \n" +
            "    org.sector as sector, \n" +
            "    org.stage as stage, \n" +
            "    org.city as city, \n" +
            "    org.state as state, \n" +
            "    org.verified as verified, \n" +
            "    org.targetMarket as targetMarket, \n" +
            "    org.rating as rating, \n" +
            "    org.logoUrl as logoUrl, \n" +
            "    COALESCE(org.inceptionYear, 0) as inceptionYear, \n" +
            "    org.lastUpdatedTimestamp as lastUpdatedTimestamp \n" +
            "FROM \n" +
            "    Organization org\n" +
            "LEFT JOIN \n" +
            "    org.preferredSectors preferredSectors\n" +
            "LEFT JOIN \n" +
            "    org.preferredPartnershipTypes preferredPartnershipTypes\n" +
            "WHERE \n" +
            "  org.id not in :queryingOrganizationId \n" +
            "    AND org.status = :status \n" +
            "ORDER BY \n" +
            "    org.lastUpdatedTimestamp DESC\n" +
            "  limit :limit")
    List<OrganizationSearchResponse> searchRandomOrganizationList(Set<Long> queryingOrganizationId, OrganizationStatus status, int limit);

    @Query(value = "SELECT DISTINCT\n" +
            "    org.id as id,  \n" +
            "    org.code as code, \n" +
            "    org.name as name, \n" +
            "    org.about as about, \n" +
            "    org.briefDescription as briefDescription, \n" +
            "    org.sector as sector, \n" +
            "    org.stage as stage, \n" +
            "    org.city as city, \n" +
            "    org.state as state, \n" +
            "    org.verified as verified, \n" +
            "    org.targetMarket as targetMarket, \n" +
            "    org.rating as rating, \n" +
            "    org.logoUrl as logoUrl, \n" +
            "    COALESCE(org.inceptionYear, 0) as inceptionYear, \n" +
            "    org.lastUpdatedTimestamp as lastUpdatedTimestamp \n" +
            "FROM \n" +
            "    Organization org\n" +
            "LEFT JOIN \n" +
            "    org.preferredSectors preferredSectors\n" +
            "LEFT JOIN \n" +
            "    org.preferredPartnershipTypes preferredPartnershipTypes\n" +
            "LEFT JOIN \n" +
            "    org.preferredSubSectors preferredSubSectors\n" +
            "WHERE \n" +
            "     org.id in :partnerId \n" +
            "    AND org.status = :status \n" +
            "ORDER BY \n" +
            "    org.lastUpdatedTimestamp DESC\n")
    Page<OrganizationSearchResponse> getBookmarkOrganizationDetails(OrganizationStatus status, List<Long> partnerId, Pageable pageable);

    @Query(value = """
            SELECT
                org.name as name,
                org.logoUrl as logoUrl,
                org.briefDescription as description
            FROM
                Organization org
            WHERE
                org.id = :organizationId
              
            """)
    OrgEmailHistoryResponse getOrgNameAndDescriptionAndLogoUrl(Long organizationId);

    @Query("SELECT o.id From Organization o WHERE o.primaryEmail  = :email")
    Long findIdByEmail(String email);

    @Query("SELECT o.id From Organization o WHERE o.primaryEmailVerified  = 'false' and o.status=0")
        //@Query("SELECT o.id From Organization o WHERE o.primaryEmail  = 'duhuciguqui-6261@yopmail.com'")
    List<Long> findAllUnverified();

    @Query("SELECT o.source From Organization o WHERE o.id  = :organizationId")
    Source findSourceById(Long organizationId);

    @Query("SELECT e.code FROM Organization e WHERE e.id = :id")
    String findCodeById(Long id);

    @Query("SELECT o FROM Organization o where o.status=0")
    List<Organization> getAllOrganizations();

    @Query("SELECT o.id FROM Organization o where o.status=0 and o.emailUnsubscribed = false and o.lastUpdatedTimestamp < :date")
    List<Long> getAllOrganizationsUpdatedBefore(LocalDate date);

    boolean existsByCode(String code);



    @Query(value = "SELECT DISTINCT\n" +
            "    org.id as id,  \n" +
            "    org.code as code, \n" +
            "    org.name as name, \n" +
            "    org.about as about, \n" +
            "    org.brief_description as briefDescription, \n" +
            "    org.sector as sector, \n" +
            "    org.stage as stage, \n" +
            "    org.city as city, \n" +
            "    org.state as state, \n" +
            "    org.verified as verified, \n" +
            "    org.target_market as targetMarket, \n" +
            "    org.rating as rating, \n" +
            "    org.logoUrl as logoUrl, \n" +
            "    COALESCE(org.inceptionYear, 0) as inceptionYear, \n" +
            "    org.lastUpdatedTimestamp as lastUpdatedTimestamp \n" +
            "FROM \n" +
            "    Organization org\n" +
            "WHERE \n" +
            "    org.id IN :organizationId AND org.status = :status\n" +
            "ORDER BY FIELD(org.id, :organizationId)", nativeQuery = true)
    List<OrganizationSearchResponse> findOrganizationById(List<Long> organizationId, OrganizationStatus status);


    Organization findByForm(String formKey);

    List<Organization> findAllByStatusAndFiltersAdded(OrganizationStatus status, boolean filtersAdded);

    List<Organization> findAllByStatus(OrganizationStatus status);

    @Query(value = "SELECT DISTINCT " +
            "    org.id as id,  " +
            "    org.code as code, " +
            "    org.name as name, " +
            "    org.about as about, " +
            "    org.briefDescription as briefDescription, " +
            "    org.sector as sector, " +
            "    org.stage as stage, " +
            "    org.city as city, " +
            "    org.state as state, " +
            "    org.verified as verified, " +
            "    org.targetMarket as targetMarket, " +
            "    org.rating as rating, " +
            "    org.logoUrl as logoUrl, " +
            "    org.sectorType as sectorType, " +
            "    COALESCE(org.inceptionYear, 0) as inceptionYear, " +
            "    org.lastUpdatedTimestamp as lastUpdatedTimestamp, " +
            "    org.companyType as companyType, " +
            "    org.acknowledgmentTime as acknowledgmentTime, " +
            "    COALESCE(org.activePartnerships, 0) as activePartnerships, " +
            "    COALESCE(org.pipelinePartnerships, 0) as pipelinePartnerships " +
            "FROM Organization org " +
            "LEFT JOIN org.preferredSectors preferredSectors " +
            "LEFT JOIN org.preferredPartnershipTypes preferredPartnershipTypes " +
            "LEFT JOIN org.preferredSubSectors preferredSubSectors " +
            "WHERE (:ignoreStage = true OR upper(org.stage) in :stages) " +
            "  AND (:ignoreSector = true OR upper(preferredSectors.area) in :sectors) " +
            "  AND (:ignoreSubSectors = true OR upper(preferredSubSectors.area) in :subSectors) " +
            "  AND (:ignorePartnershipTypes = true OR upper(preferredPartnershipTypes.area) in :partnershipTypes) ",
            countQuery = "SELECT COUNT(DISTINCT org.id) " +
                    "FROM Organization org " +
                    "LEFT JOIN org.preferredSectors preferredSectors " +
                    "LEFT JOIN org.preferredPartnershipTypes preferredPartnershipTypes " +
                    "LEFT JOIN org.preferredSubSectors preferredSubSectors " +
                    "WHERE (:ignoreStage = true OR upper(org.stage) in :stages) " +
                    "  AND (:ignoreSector = true OR upper(preferredSectors.area) in :sectors) " +
                    "  AND (:ignoreSubSectors = true OR upper(preferredSubSectors.area) in :subSectors) " +
                    "  AND (:ignorePartnershipTypes = true OR upper(preferredPartnershipTypes.area) in :partnershipTypes) ")
    Page<OrganizationSearchResponse> searchPartialOrganizations(
            List<String> stages, boolean ignoreStage,
            List<String> sectors, boolean ignoreSector,
            List<String> partnershipTypes, boolean ignorePartnershipTypes,
            List<String> subSectors, boolean ignoreSubSectors,
            Pageable pageable);

    @Query("SELECT DISTINCT o FROM Organization o " +
            "LEFT JOIN o.preferredSubSectors sub " +
            "WHERE (:subSectors IS NULL OR sub.area IN :subSectors)")
    Page<Organization> findByPreferredSubSectors(
            @Param("subSectors") java.util.List<String> subSectors,
            Pageable pageable
    );


    @Query("""
    SELECT DISTINCT org.id AS id,
           org.code AS code,
           org.name AS name,
           org.about AS about,
           org.briefDescription AS briefDescription,
           org.sector AS sector,
           org.stage AS stage,
           org.city AS city,
           org.state AS state,
           org.verified AS verified,
           COALESCE(org.inceptionYear, 0) AS inceptionYear,
           org.targetMarket AS targetMarket,
           org.rating AS rating,
           org.logoUrl AS logoUrl,
           org.sectorType AS sectorType,
           org.companyType AS companyType,
           org.acknowledgmentTime AS acknowledgmentTime,
           org.activePartnerships AS activePartnerships,
           org.pipelinePartnerships AS pipelinePartnerships
    FROM Organization org
    WHERE org.id IN :organizationId
      AND org.status = :status
    ORDER BY org.id
""")
    List<OrganizationSearchResponse> findOrganizationByIDS(
            @Param("organizationId") List<Long> organizationId,
            @Param("status") OrganizationStatus status
    );

    List<Organization> findByIdInAndStatus(List<Long> ids, OrganizationStatus status);

    List<Organization> findByIdIn(List<Long> ids);


    Page<Organization> findAll(Specification<Organization> spec, Pageable pageable);

    @Query("""
        SELECT 
            o.primaryEmail AS primaryEmail,
            o.name AS name,
            o.lastUpdatedTimestamp AS lastUpdatedTimestamp
        FROM Organization o
        WHERE o.lastUpdatedTimestamp BETWEEN :fromDate AND :toDate
        ORDER BY o.lastUpdatedTimestamp DESC
    """)
    List<OrganizationUpdateView> findUpdatedOrganizationsBetweenDates(
            @Param("fromDate") Date fromDate,
            @Param("toDate") Date toDate
    );

    List<Organization> findByWebsiteIsNotNull();
}