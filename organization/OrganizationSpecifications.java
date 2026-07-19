package com.sharkdom.entity.organization;

import com.sharkdom.constants.organization.OrganizationStatus;
import com.sharkdom.entity.catalogue.PartnerTier;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class OrganizationSpecifications {

    public static Specification<Organization> hasStatus(OrganizationStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Organization> hasFilterIn(List<String> filters) {
        return (root, query, cb) -> {
            if (filters == null || filters.isEmpty()) {
                return cb.conjunction();
            }
            List<Predicate> predicates = new ArrayList<>();
            for (String filter : filters) {
                predicates.add(
                        cb.like(cb.lower(root.get("filters")), "%" + filter.toLowerCase() + "%")
                );
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Organization> hasPreferredSectorIn(List<String> sectors) {
        return (root, query, cb) -> {
            if (sectors == null || sectors.isEmpty()) {
                return cb.conjunction();
            }
            Join<Object, Object> join = root.join("preferredSectors", JoinType.LEFT);
            Expression<String> area = cb.lower(join.get("area"));
            List<String> lowered = sectors.stream().map(String::toLowerCase).toList();
            return area.in(lowered);
        };
    }

    public static Specification<Organization> hasPreferredPartnershipIn(List<String> partnershipTypes) {
        return (root, query, cb) -> {
            if (partnershipTypes == null || partnershipTypes.isEmpty()) {
                return cb.conjunction();
            }
            Join<Object, Object> join = root.join("preferredPartnershipTypes", JoinType.LEFT);
            Expression<String> area = cb.lower(join.get("area"));
            List<String> lowered = partnershipTypes.stream().map(String::toLowerCase).toList();
            return area.in(lowered);
        };
    }

    public static Specification<Organization> hasNameLike(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Organization> hasKeywordInMultipleFields(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("about")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("briefDescription")), likePattern)
            );
        };
    }

    public static Specification<Organization> hasValidLogoUrl() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(
                        root.get("logoUrl"),
                        "https://s3.ap-south-1.amazonaws.com/sharkdom.co.in/logos/placeholder.png"
                );
    }

    public static Specification<Organization> hasSectorType(String sectorType) {
        return (root, query, cb) -> {
            if (sectorType == null || sectorType.isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("sectorType"), sectorType.toUpperCase());
        };
    }

    public static Specification<Organization> hasAtLeastOnePartnerTier() {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<PartnerTier> tierRoot = subquery.from(PartnerTier.class);

            subquery.select(cb.literal(1L))
                    .where(
                            cb.equal(tierRoot.get("orgId"), root.get("id")),
                            cb.isTrue(tierRoot.get("active")) // optional
                    );

            return cb.exists(subquery);
        };
    }

    public static Specification<Organization> hasComplianceIn(List<String> compliances) {
        return (root, query, cb) -> {
            if (compliances == null || compliances.isEmpty()) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            for (String compliance : compliances) {
                predicates.add(cb.like(
                        cb.lower(root.get("compliances").as(String.class)),
                        "%" + compliance.toLowerCase() + "%"
                ));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Organization> hasMostPopular(Long mostPopular) {
        return (root, query, cb) -> {
            if (mostPopular == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("mostPopular"), mostPopular);
        };
    }

    public static Specification<Organization> hasTopPartner(Long topPartner) {
        return (root, query, cb) -> {
            if (topPartner == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("topPartner"), topPartner);
        };
    }

    public static Specification<Organization> hasLowAcknowledgmentTime(Boolean isLowAcknowledgeTime) {
        return (root, query, cb) -> {
            if (isLowAcknowledgeTime == null || !isLowAcknowledgeTime) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(root.get("acknowledgmentTime"), 24L);
        };
    }

    public static Specification<Organization> isTopPartner(Boolean isTopPartner) {
        return (root, query, cb) -> {
            if (isTopPartner == null || !isTopPartner) {
                return cb.conjunction();
            }

            return cb.equal(root.get("topPartner"), 1L);
        };
    }

    public static Specification<Organization> isPopular(Boolean isPopular) {
        return (root, query, cb) -> {
            if (isPopular == null || !isPopular) {
                return cb.conjunction();
            }

            return cb.equal(root.get("mostPopular"), 1L);
        };
    }

    public static Specification<Organization> isMostActive(Boolean isMostActive) {
        return (root, query, cb) -> {
            if (isMostActive == null || !isMostActive) {
                return cb.conjunction();
            }

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -30);

            return cb.greaterThanOrEqualTo(root.get("lastActivityAtTimestamp"), cal.getTime());
        };
    }

    public static Specification<Organization> isShortlisted(Boolean isShortlisted, Long currentOrgId) {
        return (root, query, cb) -> {
            if (isShortlisted == null || !isShortlisted) {
                return cb.conjunction();
            }

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<ShortlistOrganization> shortlist = subquery.from(ShortlistOrganization.class);

            subquery.select(cb.literal(1L))
                    .where(
                            cb.equal(shortlist.get("shortlistedOrgId"), root.get("id")),
                            cb.equal(shortlist.get("shortlistedByOrgId"), currentOrgId)
                    );

            return cb.exists(subquery);
        };
    }
}

