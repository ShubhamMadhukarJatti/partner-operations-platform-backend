package com.sharkdom.repository.user;

import com.sharkdom.entity.user.UserOtherConnections;
import com.sharkdom.model.user.UserConnectionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOtherConnectionsRepository extends JpaRepository<UserOtherConnections, Long> {

    @Query(value = "select user_id as userId, \n" +
            "(select u.username from users u where u.user_id = c.user_id) as username, \n" +
            "connection_user_id as connectionUserId, \n" +
            "(select u.username from users u where u.user_id = c.user_id) as connectionUsername, \n" +
            "  CASE status\n" +
            "      WHEN '0' THEN 'PENDING'\n" +
            "      WHEN '1' THEN 'REJECTED'\n" +
            "      WHEN '2' THEN 'BLOCKED'\n" +
            "      WHEN '3' THEN 'ACTIVE'\n" +
            "      ELSE NULL\n" +
            "  END as status \n" +
            "from user_other_connections c \n" +
            "WHERE ( c.user_id = :userId and c.connection_user_id = :connectionUserId) \n" +
            "OR ( c.user_id = :connectionUserId and c.connection_user_id = :userId)", nativeQuery = true)
    Optional<UserConnectionModel> findByUserIdAndConnectionUserIdAsModel(String userId, String connectionUserId);

    @Query("SELECT u FROM UserOtherConnections u "
            + "WHERE ( u.userId = :userId and u.connectionUserId = :connectionUserId) "
            + "OR ( u.userId = :connectionUserId and u.connectionUserId = :userId)")
    Optional<UserOtherConnections> findByUserIdAndConnectionUserId(String userId, String connectionUserId);


    @Query(value = "select user_id as userId, \n" +
            "(select u.username from users u where u.user_id = c.user_id) as username, \n" +
            "connection_user_id as connectionUserId, \n" +
            "(select u.username from users u where u.user_id = c.user_id) as connectionUsername, \n" +
            "  CASE status\n" +
            "      WHEN '0' THEN 'PENDING'\n" +
            "      WHEN '1' THEN 'REJECTED'\n" +
            "      WHEN '2' THEN 'BLOCKED'\n" +
            "      WHEN '3' THEN 'ACTIVE'\n" +
            "      ELSE NULL\n" +
            "  END as status \n" +
            "from user_other_connections c \n" +
            "where c.user_id = :userId \n" +
            "or c.connection_user_id = :connectionUserId", nativeQuery = true)
    Page<UserConnectionModel> findAllByUserIdOrConnectionUserId(String userId, String connectionUserId,
                                                                Pageable pageable);

    void deleteByUserIdAndConnectionUserId(String userId, String connectionUserId);

}
