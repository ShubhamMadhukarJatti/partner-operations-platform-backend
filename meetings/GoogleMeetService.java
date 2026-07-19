package com.sharkdom.service.meetings;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

@Service
public class GoogleMeetService {
    @Value("${google.client_id}")
    private String clientId;
    @Value("${google.client_secret}")
    private String clientSecret;

    private static final String APPLICATION_NAME = "Google Calendar API";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public String createEvent(String refreshToken, String title, String description, String startDateTime, String endDateTime, List<String> attendeeEmails) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        HttpCredentialsAdapter requestInitializer = new HttpCredentialsAdapter(credentials);

        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, requestInitializer)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Event event = new Event()
                .setSummary(title)
                .setDescription(description)
                .setStart(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startDateTime)))
                .setEnd(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(endDateTime)));

        // Add attendees
        if (attendeeEmails != null && !attendeeEmails.isEmpty()) {
            List<EventAttendee> attendees = attendeeEmails.stream()
                    .map(email -> new EventAttendee().setEmail(email))
                    .toList();
            event.setAttendees(attendees);
        }

        // Add conference (Google Meet link)
        ConferenceData conferenceData = new ConferenceData();
        CreateConferenceRequest createConferenceRequest = new CreateConferenceRequest();
        createConferenceRequest.setRequestId(UUID.randomUUID().toString()); // Unique request ID
        conferenceData.setCreateRequest(createConferenceRequest);
        event.setConferenceData(conferenceData);

        // Insert the event with conferenceDataVersion set to 1
        Event createdEvent = service.events()
                .insert("primary", event)
                .setConferenceDataVersion(1) // Ensures conference data (Meet link) is generated
                .execute();

        return createdEvent.getHtmlLink(); // Returns the event link
    }

}
