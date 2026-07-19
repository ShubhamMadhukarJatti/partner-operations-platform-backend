package com.sharkdom.service.ai;

import com.sharkdom.entity.ai.OverlapRecordEntity;
import com.sharkdom.model.ai.OverlapRecordsField;
import com.sharkdom.model.ai.RecordType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonaDealsCompanyContactOverlapVersion {

    private final HubspotService hubspotService;

    private enum HubspotObjectType {
        CONTACTS,
        COMPANIES,
        DEALS
    }

    public List<OverlapRecordsField> triggerForOrgAndReturnFields(OverlapRecordEntity record) {

        Map<String, String> mapping = record.getFieldToColumnMapping();

        if (mapping == null || mapping.isEmpty()) {
            log.warn("[V2_MAPPING_EMPTY]");
            return Collections.emptyList();
        }

        HubspotObjectType objectType = resolveObjectType(record.getRecordType());

        String properties = String.join(",", mapping.values());

        Map<Object, Object> response =
                hubspotService.getObjectData(
                        record.getOrganizationId(),
                        objectType.name(),
                        properties
                );

        return buildFields(response, mapping, objectType);
    }

    private HubspotObjectType resolveObjectType(RecordType recordType) {
        return switch (recordType) {
            case CUSTOMER -> HubspotObjectType.COMPANIES;
            case PROSPECT -> HubspotObjectType.CONTACTS;
            case OPPORTUNITY -> HubspotObjectType.DEALS;
            default -> throw new RuntimeException("Unsupported type");
        };
    }

    private List<OverlapRecordsField> buildFields(
            Map<Object, Object> response,
            Map<String, String> mapping,
            HubspotObjectType type) {

        Object resultsObj = response.get("results");

        if (!(resultsObj instanceof List<?> results)) {
            return Collections.emptyList();
        }

        List<OverlapRecordsField> list = new ArrayList<>();

        for (Object obj : results) {

            if (!(obj instanceof Map<?, ?> recordMap)) continue;

            Object propertiesObj = recordMap.get("properties");

            if (!(propertiesObj instanceof Map<?, ?> props)) continue;

            OverlapRecordsField f = new OverlapRecordsField();

            switch (type) {

                case CONTACTS -> mapContact(f, props, mapping);
                case COMPANIES -> mapCompany(f, props, mapping);
                case DEALS -> mapDeal(f, props, mapping);
            }

            list.add(f);
        }

        log.info("[V2_FIELDS_CREATED] count={}", list.size());

        return list;
    }

    // CONTACT
    private void mapContact(OverlapRecordsField f, Map<?, ?> p, Map<String, String> m) {
        f.setContactEmail(get(p, m.get("contactEmail")));
        f.setFirstName(get(p, m.get("firstName")));
        f.setLastName(get(p, m.get("lastName")));
        f.setJobTitle(get(p, m.get("jobTitle")));
        f.setContactLinkedinUrl(get(p, m.get("contactLinkedinUrl")));
        f.setLeadStatus(get(p, m.get("leadStatus")));
        f.setContactPhone(get(p, m.get("contactPhone")));
        f.setLastActivityDate(get(p, m.get("lastActivityDate")));
        f.setContactOwner(get(p, m.get("contactOwner")));
        f.setAssociatedCompanyId(get(p, m.get("associatedCompanyId")));
    }

    // COMPANY
    private void mapCompany(OverlapRecordsField f, Map<?, ?> p, Map<String, String> m) {
        f.setCompanyName(get(p, m.get("companyName")));
        f.setWebsite(get(p, m.get("website")));
        f.setIndustry(get(p, m.get("industry")));
        f.setCompanySize(get(p, m.get("companySize")));
        f.setCountry(get(p, m.get("country")));
        f.setLinkedinUrl(get(p, m.get("linkedinUrl")));
        f.setAnnualRevenue(get(p, m.get("annualRevenue")));
        f.setDescription(get(p, m.get("description")));
        f.setCompanyPhone(get(p, m.get("companyPhone")));
        f.setCity(get(p, m.get("city")));
    }

    // DEAL
    private void mapDeal(OverlapRecordsField f, Map<?, ?> p, Map<String, String> m) {
        f.setDealName(get(p, m.get("dealName")));
        f.setDealStage(get(p, m.get("dealStage")));
        f.setAssociatedCompanyId(get(p, m.get("associatedCompanyId")));
        f.setDealOwner(get(p, m.get("dealOwner")));
        f.setAmountAcv(get(p, m.get("amountAcv")));
        f.setCloseDate(get(p, m.get("closeDate")));
        f.setDealId(get(p, m.get("dealId")));
        f.setPipeline(get(p, m.get("pipeline")));
        f.setLastActivityDate(get(p, m.get("lastActivityDate")));
        f.setDealType(get(p, m.get("dealType")));
        f.setAssociatedContactId(get(p, m.get("associatedContactId")));
    }

    private String get(Map<?, ?> map, String key) {
        if (key == null) return null;
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}