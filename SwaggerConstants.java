package com.sharkdom.constants;

public interface SwaggerConstants {
    String CREATE_MEETING = "{\n" +
            "  \"senderOrganizationId\": 2,\n" +
            "  \"receiverOrganizationId\": 5,\n" +
            "  \"title\": \"title of meeting\",\n" +
            "  \"description\": \"description of meeting\",\n" +
            "  \"availability\": [\n" +
            "    {\n" +
            "      \"time\": \"2024-04-06T14:28:08.962Z\"\n" +
            "    },\n" +
            " {\n" +
            "      \"time\": \"2024-04-06T15:28:08.962Z\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    String RESCHEDULE_MEETING = "{\n" +
            "  \"id\": 1,\n" +
            "  \"meetingTime\": \"2024-04-06T15:01:22.218Z\",\n" +
            "  \"rescheduledBy\": 2\n" +
            "}";

    String ACCEPT_MEETING = "{\n" +
            "  \"id\": 1,\n" +
            "  \"meetingTime\": \"2024-04-06T15:09:10.627Z\"\n" +
            "}";

    String CANCEL_MEETING = "{\n" +
            "  \"id\": 1,\n" +
            "  \"cancelledBy\": 2\n" +
            "}";

    String PATCH_INTEGRATION =
            """ 
                            {
                             "organizationId": 1,
                             "refreshToken": "string1",
                             "integrationType": "HUBSPOT"
                           }
                           
                    """;
}
