package com.sharkdom.repository.user;

import com.sharkdom.entity.user.Collab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollabRequestRepository extends JpaRepository<Collab, Long> {

    List<Collab> findAllByUserId(String userId);

    List<Collab> findAllByReceiverUserId(String receiverUserId);

    @Query(value = "Select user_id, count(1) " +
            "from collab c " +
            "where c.user_id in :userIds " +
            "or c.receiver_user_id in :receiverUserIds " +
            "group by user_id",
            nativeQuery = true)
    List<Object[]> getCountByReceiverUserIdsInOrUserIdsIn(List<String> receiverUserIds, List<String> userIds);

    public void deleteByUserId(String userId);

}