package com.sharkdom.model.meetings;

import com.sharkdom.constants.meeting.MeetingApps;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record MeetingEventResponse(
        Long id,
        LocalDateTime formTime,
        LocalDateTime toTime,
        SenderOrganizationDetail senderOrganizationDetail,
        ReceiverOrganizationDetail receiverOrganizationDetail,
        MeetingApps meetingApp,
        String meetLink,
        LocalDate meetDate) {
}
