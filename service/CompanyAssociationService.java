package com.sharkdom.partnerattribution.service;

import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompanyAssociationService {

    /**
     * COMPANY -> CONTACT
     *
     * Rule:
     * Contact belongs to same company
     *
     * Action:
     * No Action (already won)
     */
    public List<OverlapRecordFieldEntity> mapCompanyToContact(
            List<OverlapRecordFieldEntity> companies,
            List<OverlapRecordFieldEntity> contacts
    ) {
        log.info(
                "Started Company -> Contact mapping. companies={}, contacts={}",
                companies.size(),
                contacts.size()
        );

        Set<String> companyIds = companies.stream()
                .map(OverlapRecordFieldEntity::getAssociatedCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<OverlapRecordFieldEntity> overlaps = contacts.stream()
                .filter(contact ->
                        contact.getAssociatedCompanyId() != null
                                && companyIds.contains(
                                contact.getAssociatedCompanyId()
                        )
                )
                .toList();

        log.info(
                "Completed Company -> Contact mapping. matched={}",
                overlaps.size()
        );

        return overlaps;
    }

    /**
     * COMPANY -> DEAL
     *
     * Rule:
     * Deal belongs to same company
     *
     * Action:
     * Co-sell on Expansion (pursuit)
     */
    public List<OverlapRecordFieldEntity> mapCompanyToDeal(
            List<OverlapRecordFieldEntity> companies,
            List<OverlapRecordFieldEntity> deals
    ) {
        log.info(
                "Started Company -> Deal mapping. companies={}, deals={}",
                companies.size(),
                deals.size()
        );

        Set<String> companyIds = companies.stream()
                .map(OverlapRecordFieldEntity::getAssociatedCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<OverlapRecordFieldEntity> overlaps = deals.stream()
                .filter(deal ->
                        deal.getAssociatedCompanyId() != null
                                && companyIds.contains(
                                deal.getAssociatedCompanyId()
                        )
                )
                .toList();

        log.info(
                "Completed Company -> Deal mapping. matched={}",
                overlaps.size()
        );

        return overlaps;
    }

    /**
     * COMPANY -> COMPANY
     *
     * Rule:
     * Same company exists in both org and partner
     *
     * Action:
     * Joint Customer (advocacy/case study)
     */
    public List<OverlapRecordFieldEntity> mapCompanyToCompany(
            List<OverlapRecordFieldEntity> organizationCompanies,
            List<OverlapRecordFieldEntity> partnerCompanies
    ) {
        log.info(
                "Started Company -> Company mapping. orgCompanies={}, partnerCompanies={}",
                organizationCompanies.size(),
                partnerCompanies.size()
        );

        Set<String> companyIds = organizationCompanies.stream()
                .map(OverlapRecordFieldEntity::getAssociatedCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<OverlapRecordFieldEntity> overlaps = partnerCompanies.stream()
                .filter(company ->
                        company.getAssociatedCompanyId() != null
                                && companyIds.contains(
                                company.getAssociatedCompanyId()
                        )
                )
                .toList();

        log.info(
                "Completed Company -> Company mapping. matched={}",
                overlaps.size()
        );

        return overlaps;
    }
}