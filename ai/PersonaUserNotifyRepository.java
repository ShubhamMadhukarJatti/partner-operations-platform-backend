package com.sharkdom.repository.ai;

import com.sharkdom.entity.ai.PersonaUserNotifyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonaUserNotifyRepository extends JpaRepository<PersonaUserNotifyEntity, Long> {
    Optional<PersonaUserNotifyEntity> findBySenderOrgIdAndRecieverOrgId(Long senderOrgId, Long recieverOrgId);



    List<PersonaUserNotifyEntity> findAllBySenderOrgId(Long senderOrgId);

    List<PersonaUserNotifyEntity> findAllByRecieverOrgId(long recieverOrgId);


    List<PersonaUserNotifyEntity> findAllByRecieverOrgIdAndIsNotified(Long recieverOrgId, Boolean isNotified);

}
