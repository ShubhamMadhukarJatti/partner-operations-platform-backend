package com.sharkdom.zoho.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZohoModuleRouter {

    private final ContactSyncService contactSyncService;

    private final DealSyncService dealSyncService;

    private final AccountSyncService accountSyncService;

    public void process(

            Long tenantId,

            String module,

            Map<String, Object> record

    ) {

        switch (module) {

            case "Contacts" ->
                    contactSyncService.sync(
                            tenantId,
                            record
                    );

            case "Deals" ->
                    dealSyncService.sync(
                            tenantId,
                            record
                    );

            case "Accounts" ->
                    accountSyncService.sync(
                            tenantId,
                            record
                    );

            default ->
                    throw new RuntimeException(
                            "Unsupported module: "
                                    + module
                    );
        }
    }
}