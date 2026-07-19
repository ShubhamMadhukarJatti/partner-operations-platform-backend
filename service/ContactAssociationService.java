package com.sharkdom.partnerattribution.service;

import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ContactAssociationService {

    /**
     * CONTACT -> CONTACT
     *
     * Rule:
     * Same associated company = same account touchpoint
     *
     * Action:
     * Monitor (low-priority engagement)
     */
    public List<OverlapRecordFieldEntity> mapContactToContact(
            List<OverlapRecordFieldEntity> organizationContacts,
            List<OverlapRecordFieldEntity> partnerContacts
    ) {
        log.info(
                "Started Contact -> Contact mapping. orgContacts={}, partnerContacts={}",
                organizationContacts.size(),
                partnerContacts.size()
        );

        Set<String> companyIds = organizationContacts.stream()
                .map(OverlapRecordFieldEntity::getAssociatedCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<OverlapRecordFieldEntity> overlaps = partnerContacts.stream()
                .filter(contact ->
                        contact.getAssociatedCompanyId() != null
                                && companyIds.contains(
                                contact.getAssociatedCompanyId()
                        )
                )
                .toList();

        log.info(
                "Completed Contact -> Contact mapping. matched={}",
                overlaps.size()
        );

        return overlaps;
    }

    /**
     * CONTACT -> DEAL
     *
     * Rule:
     * Contact company exists in deal company
     *
     * Action:
     * Add to Pipeline (partner validation)
     */
    public List<OverlapRecordFieldEntity> mapContactToDeal(
            List<OverlapRecordFieldEntity> contacts,
            List<OverlapRecordFieldEntity> deals
    ) {
        log.info(
                "Started Contact -> Deal mapping. contacts={}, deals={}",
                contacts.size(),
                deals.size()
        );

        Set<String> companyIds = contacts.stream()
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
                "Completed Contact -> Deal mapping. matched={}",
                overlaps.size()
        );

        return overlaps;
    }

    /**
     * CONTACT -> COMPANY
     *
     * Rule:
     * Contact belongs to same company
     *
     * Action:
     * Request Intro (warm door access)
     */
    public List<OverlapRecordFieldEntity> mapContactToCompany(
            List<OverlapRecordFieldEntity> contacts,
            List<OverlapRecordFieldEntity> companies
    ) {
        log.info(
                "Started Contact -> Company mapping. contacts={}, companies={}",
                contacts.size(),
                companies.size()
        );

        Set<String> companyIds = contacts.stream()
                .map(OverlapRecordFieldEntity::getAssociatedCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<OverlapRecordFieldEntity> overlaps = companies.stream()
                .filter(company ->
                        company.getAssociatedCompanyId() != null
                                && companyIds.contains(
                                company.getAssociatedCompanyId()
                        )
                )
                .toList();

        log.info(
                "Completed Contact -> Company mapping. matched={}",
                overlaps.size()
        );

        return overlaps;
    }
}