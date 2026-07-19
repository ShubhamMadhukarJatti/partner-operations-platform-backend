package com.sharkdom.service.user;

import com.sharkdom.entity.user.ProfileVisits;
import com.sharkdom.repository.user.ProfileVisitsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class ProfileVisitsService {

    @Autowired
    ProfileVisitsRepository profileVisitsRepository;

    public List<ProfileVisits> findAllProfileVisitsByVisitedUserIdAndAndCreationTimestampBetween(String visitedUserId,
                                                                                                 Date fromTimestamp, Date toTimeStamp) {
        return profileVisitsRepository.findAllProfileVisitsByVisitedUserIdAndAndCreationTimestampBetween(visitedUserId,
                fromTimestamp, toTimeStamp);
    }

    @Transactional
    public ProfileVisits createOrUpdate(ProfileVisits profileVisits) {
        return profileVisitsRepository.save(profileVisits);
    }
}
