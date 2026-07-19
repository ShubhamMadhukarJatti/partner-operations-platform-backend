package com.sharkdom.mapper.meeting;

import com.sharkdom.entity.meetings.MeetingEvent;
import com.sharkdom.entity.organization.Organization;
import com.sharkdom.model.meetings.MeetingEventResponse;
import com.sharkdom.model.meetings.ReceiverOrganizationDetail;
import com.sharkdom.model.meetings.SenderOrganizationDetail;

public class MeetingEventMapper {

    private MeetingEventMapper(){
    }

    public static MeetingEventResponse mapMeetingEventToMeetingEventResponse(MeetingEvent meetingEvent, Organization receiverOrganization, Organization senderOrganization) {
        return MeetingEventResponse.builder()
                .id(meetingEvent.getId())
                .meetingApp(meetingEvent.getMeetingApp())
                .meetLink(meetingEvent.getMeetLink())
                .meetDate(meetingEvent.getMeetDate())
                .formTime(meetingEvent.getStartDateTime())
                .toTime(meetingEvent.getEndDateTime())
                .senderOrganizationDetail(mapSenderOrganizationDetail(senderOrganization))
                .receiverOrganizationDetail(mapReceiverOrganizationDetail(receiverOrganization))
                .build();
    }

    private static ReceiverOrganizationDetail mapReceiverOrganizationDetail(Organization receiverOrganization) {
        return ReceiverOrganizationDetail.builder()
                .organizationId(receiverOrganization.getId())
                .logoUrl(receiverOrganization.getLogoUrl())
                .build();
    }

    private static SenderOrganizationDetail mapSenderOrganizationDetail(Organization senderOrganization) {
        return SenderOrganizationDetail.builder()
                .organizationId(senderOrganization.getId())
                .logoUrl(senderOrganization.getLogoUrl())
                .build();
    }

}
