package com.sharkdom.agenticai.specification;

import com.sharkdom.agenticai.entity.PartnerCompanyProfile;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class PartnerCompanyProfileSpecification {

    public static Specification<PartnerCompanyProfile> hasSubsectorIn(List<String> subsectors) {
        return (root, query, cb) -> {
            if (subsectors == null || subsectors.isEmpty()) return null;

            return subsectors.stream()
                    .map(sub -> cb.like(cb.lower(root.get("subsectors")), "%" + sub.toLowerCase() + "%"))
                    .reduce(cb::or)
                    .orElse(null);
        };
    }

    public static Specification<PartnerCompanyProfile> hasComplianceIn(List<String> compliances) {
        return (root, query, cb) -> {
            if (compliances == null || compliances.isEmpty()) return null;

            return compliances.stream()
                    .map(c -> cb.like(cb.lower(root.get("compliances")), "%" + c.toLowerCase() + "%"))
                    .reduce(cb::or)
                    .orElse(null);
        };
    }

    public static Specification<PartnerCompanyProfile> hasKeyword(String keyword) {
        return (root, query, cb) -> {

            if (keyword == null || keyword.isEmpty()) return null;

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("companyName")), like),
                    cb.like(cb.lower(root.get("description")), like),
                    cb.like(cb.lower(root.get("about")), like)
            );
        };
    }

}