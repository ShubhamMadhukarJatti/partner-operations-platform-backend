package com.sharkdom.model.typeform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypeFormWebhookResponse {

    private String createdAt;
    private boolean enabled;
    private EventTypes eventTypes;
    private String formId;
    private String id;
    private String tag;
    private String updatedAt;
    private String url;
    private boolean verifyssl;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EventTypes {
        private boolean formResponse;
        private boolean formResponsePartial;
    }
}
