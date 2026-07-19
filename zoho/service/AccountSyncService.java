package com.sharkdom.zoho.service;

import com.sharkdom.zoho.entity.ZohoCrmAccount;
import com.sharkdom.zoho.repository.CrmAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountSyncService {

    private final CrmAccountRepository repository;

    public void sync(

            Long tenantId,

            Map<String, Object> record

    ) {

        String recordId =
                String.valueOf(record.get("id"));

        ZohoCrmAccount account =
                repository
                        .findByZohoRecordId(recordId)
                        .orElse(new ZohoCrmAccount());

        account.setTenantId(tenantId);

        account.setZohoRecordId(recordId);

        account.setAccountName(
                (String) record.get("Account_Name")
        );

        account.setWebsite(
                (String) record.get("Website")
        );

        account.setPhone(
                (String) record.get("Phone")
        );

        account.setIndustry(
                (String) record.get("Industry")
        );

        repository.save(account);
    }
}