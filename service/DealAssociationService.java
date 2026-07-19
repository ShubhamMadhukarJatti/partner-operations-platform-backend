package com.sharkdom.partnerattribution.service;

import com.sharkdom.entity.ai.OverlapRecordFieldEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DealAssociationService {

    /**
     * Deal -> Deal overlap
     * Match partner deals with organization deals
     * by associatedCompanyId
     */
    public List<OverlapRecordFieldEntity> mapDealToDeal(
            List<OverlapRecordFieldEntity> organizationDeals,
            List<OverlapRecordFieldEntity> partnerDeals
    ) {
        log.info(
                "Started Deal -> Deal mapping. orgDeals={}, partnerDeals={}",
                organizationDeals.size(),
                partnerDeals.size()
        );

        Set<String> organizationCompanyIds = organizationDeals.stream()
                .map(OverlapRecordFieldEntity::getAssociatedCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<OverlapRecordFieldEntity> mappedDeals = partnerDeals.stream()
                .filter(deal ->
                        deal.getAssociatedCompanyId() != null
                                && organizationCompanyIds.contains(
                                deal.getAssociatedCompanyId()
                        )
                )
                .toList();

        log.info(
                "Completed Deal -> Deal mapping. matchedDeals={}",
                mappedDeals.size()
        );

        return mappedDeals;
    }

    /**
     * Deal -> Company mapping
     * Find companies linked to deals
     */
    public List<OverlapRecordFieldEntity> mapDealToCompany(
            List<OverlapRecordFieldEntity> deals,
            List<OverlapRecordFieldEntity> companies
    ) {
        log.info(
                "Started Deal -> Company mapping. deals={}, companies={}",
                deals.size(),
                companies.size()
        );

        Set<String> dealCompanyIds = deals.stream()
                .map(OverlapRecordFieldEntity::getAssociatedCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<OverlapRecordFieldEntity> mappedCompanies = companies.stream()
                .filter(company ->
                        company.getAssociatedCompanyId() != null
                                && dealCompanyIds.contains(
                                company.getAssociatedCompanyId()
                        )
                )
                .toList();

        log.info(
                "Completed Deal -> Company mapping. matchedCompanies={}",
                mappedCompanies.size()
        );

        return mappedCompanies;
    }

    /**
     * Deal -> Contact mapping
     * Match contacts either by associatedContactId
     * OR associatedCompanyId
     */
    public List<OverlapRecordFieldEntity> mapDealToContact(
            List<OverlapRecordFieldEntity> deals,
            List<OverlapRecordFieldEntity> contacts
    ) {
        log.info(
                "Started Deal -> Contact mapping. deals={}, contacts={}",
                deals.size(),
                contacts.size()
        );

        Set<String> dealContactIds = deals.stream()
                .map(OverlapRecordFieldEntity::getAssociatedContactId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> dealCompanyIds = deals.stream()
                .map(OverlapRecordFieldEntity::getAssociatedCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<OverlapRecordFieldEntity> mappedContacts = contacts.stream()
                .filter(contact ->
                        (contact.getAssociatedContactId() != null
                                && dealContactIds.contains(
                                contact.getAssociatedContactId()
                        ))
                                ||
                                (contact.getAssociatedCompanyId() != null
                                        && dealCompanyIds.contains(
                                        contact.getAssociatedCompanyId()
                                ))
                )
                .toList();

        log.info(
                "Completed Deal -> Contact mapping. matchedContacts={}",
                mappedContacts.size()
        );

        return mappedContacts;
    }
}