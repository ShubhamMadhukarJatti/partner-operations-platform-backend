package com.sharkdom.repository.meetings;

import com.sharkdom.entity.meetings.MeetingScheduleSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MeetingScheduleSettingRepository extends JpaRepository<MeetingScheduleSettings, Long> {

    Optional<MeetingScheduleSettings> findByOrganizationId(Long organizationId);

}
