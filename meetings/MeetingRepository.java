package com.sharkdom.repository.meetings;

import com.sharkdom.entity.meetings.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByOrganizationAAndOrganizationB(Long organizationA, Long organizationB);

}
