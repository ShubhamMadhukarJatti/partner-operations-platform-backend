package com.sharkdom.zoho.service;

import com.sharkdom.zoho.entity.CrmContact;
import com.sharkdom.zoho.repository.CrmContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactSyncService {

    private final CrmContactRepository repository;

    public void sync(

            Long tenantId,

            Map<String, Object> record

    ) {

        try {

            String recordId =
                    String.valueOf(record.get("id"));

            CrmContact contact =
                    repository
                            .findByZohoRecordId(recordId)
                            .orElse(new CrmContact());

            contact.setTenantId(tenantId);

            contact.setZohoRecordId(recordId);

            contact.setFirstName(
                    (String) record.get("First_Name")
            );

            contact.setLastName(
                    (String) record.get("Last_Name")
            );

            contact.setEmail(
                    (String) record.get("Email")
            );

            contact.setPhone(
                    (String) record.get("Phone")
            );

            contact.setLeadSource(
                    (String) record.get("Lead_Source")
            );

            /*
             * Owner object comes nested from Zoho
             */

            Object ownerObject =
                    record.get("Owner");

            if (ownerObject instanceof Map ownerMap) {

                Object ownerName =
                        ownerMap.get("name");

                if (ownerName != null) {

                    contact.setOwnerName(
                            ownerName.toString()
                    );
                }
            }

            repository.save(contact);

            log.info(
                    "Contact Synced Successfully: {}",
                    recordId
            );

        } catch (Exception e) {

            log.error(
                    "Contact Sync Failed",
                    e
            );
        }
    }
}