package com.sharkdom.model.ai;

public record MeetingSchedule(String userId,
                              String company,
                              String purpose,
                              String email,
                              String date,
                              String time,
                              Integer companySize,
                              String name) {
}
