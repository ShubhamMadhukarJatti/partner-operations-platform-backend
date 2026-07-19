package com.sharkdom.zoho.service;

import com.sharkdom.zoho.entity.CrmDeal;
import com.sharkdom.zoho.repository.CrmDealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DealSyncService {

    private final CrmDealRepository repository;

    public void sync(

            Long tenantId,

            Map<String, Object> record

    ) {

        String recordId =
                String.valueOf(record.get("id"));

        CrmDeal deal =
                repository
                        .findByZohoRecordId(recordId)
                        .orElse(new CrmDeal());

        deal.setTenantId(tenantId);

        deal.setZohoRecordId(recordId);

        deal.setDealName(
                (String) record.get("Deal_Name")
        );

        deal.setStage(
                (String) record.get("Stage")
        );

        Object amount =
                record.get("Amount");

        if (amount != null) {

            deal.setAmount(
                    new BigDecimal(
                            amount.toString()
                    )
            );
        }

        repository.save(deal);
    }
}