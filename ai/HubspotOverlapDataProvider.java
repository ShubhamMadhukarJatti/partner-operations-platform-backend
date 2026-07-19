package com.sharkdom.service.ai;

import com.sharkdom.model.ai.OverlapFieldRequest;
import com.sharkdom.model.ai.OverlapRequest;
import com.sharkdom.model.ai.PersonaMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HubspotOverlapDataProvider implements OverlapDataProvider {

    private final HubspotService hubspotService;

    @Override
    public PersonaMode getSource() {
        return PersonaMode.HUBSPOT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OverlapRequest fetch(Long orgId) {

        Map<String, String> mapping = getFieldMapping();

        List<String> properties =
                mapping.values().stream()
                        .filter(v -> !"dont_import".equals(v))
                        .toList();

        Map<Object, Object> response =
                hubspotService.getDetailsForVersioning(orgId, properties);

        List<OverlapFieldRequest> fields =
                mapResponse(response, mapping);

        OverlapRequest request = new OverlapRequest();
        request.setOrganizationId(orgId);
        request.setPersona(PersonaMode.HUBSPOT);
        request.setFieldToColumnMapping(mapping);
        request.setFields(fields);

        return request;
    }

    private Map<String, String> getFieldMapping() {
        return Map.of(
                "domain", "website",
                "name", "firstname",
                "companyName", "company",
                "contactEmail", "email",
                "dealStage", "dealstage",
                "creationDate", "createdate",
                "closeDate", "recent_deal_close_date",
                "subscribed", "hs_has_active_subscription",
                "ticketSize", "annualrevenue"
        );
    }

    private List<OverlapFieldRequest> mapResponse(
            Map<Object, Object> json,
            Map<String, String> mapping) {

        List<OverlapFieldRequest> list = new ArrayList<>();

        if (json == null || !json.containsKey("results"))
            return list;

        List<Map<String, Object>> results =
                (List<Map<String, Object>>) json.get("results");

        for (Map<String, Object> result : results) {

            Map<String, Object> props =
                    (Map<String, Object>) result.get("properties");

            OverlapFieldRequest field = new OverlapFieldRequest();

            mapping.forEach((internalField, hubspotKey) -> {

                if ("dont_import".equals(hubspotKey)) return;

                Object val = props.get(hubspotKey);

                setFieldValue(field, internalField,
                        val != null ? val.toString() : null);
            });

            list.add(field);
        }

        return list;
    }

    private void setFieldValue(
            OverlapFieldRequest field,
            String fieldName,
            String value) {

        try {
            var declaredField =
                    OverlapFieldRequest.class
                            .getDeclaredField(fieldName);

            declaredField.setAccessible(true);
            declaredField.set(field, value);

        } catch (Exception ignored) {}
    }
}
