package com.sharkdom.repository.user;

import com.sharkdom.entity.user.UserInterestsRaw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInterestsRawRepository extends JpaRepository<UserInterestsRaw, Long> {


    @Query(value = "select  \r\n"
            + "u.user_id AS userId,\r\n"
            + "u.name AS name ,\r\n"
            + "u.brief_description AS briefDescription,\r\n"
            + "u.user_type AS userType,\r\n"
            + "GROUP_CONCAT(DISTINCT r2.interest_area SEPARATOR '') AS interests, \r\n"
            + "u.username AS username \r\n"
            + "from user_interests_raw r1, user_interests_raw r2, users u\r\n"
            + "where r2.user_id = u.user_Id\r\n"
            + "and u.user_type = :userType \r\n"
            + "and r1.user_id = :userId \r\n" +
            " and (u.about is not null or 'false' = :filterIncompleteProfiles)\n" +
            " and (u.brief_description is not null or 'false' = :filterIncompleteProfiles) \r\n"
            + "and r1.interest_area = r2.interest_area\r\n"
            + "and r2.user_id <> r1.user_id\r\n"
            + "and r2.user_id not in (select connection_user_id from user_active_connections where user_id = r1.user_id) "
            + "and r2.user_id not in (select user_id from user_active_connections where connection_user_id = r1.user_id) "
            + "and r2.user_id not in (select connection_user_id from user_other_connections where user_id = r1.user_id) "
            + "and r2.user_id not in (select user_id from user_other_connections where connection_user_id = r1.user_id) "
            + "group by u.user_Id", nativeQuery = true)
    public List<Object[]> findRecommendationsByUserIdAndUserType(String userId, String userType, boolean filterIncompleteProfiles);


    @Query(value = "select  \r\n"
            + "u.user_id AS userId,\r\n"
            + "u.name AS name ,\r\n"
            + "u.brief_description AS briefDescription,\r\n"
            + "u.user_type AS userType,\r\n"
            + "GROUP_CONCAT(DISTINCT r2.interest_area SEPARATOR '') AS interests, \r\n"
            + "u.username AS username \r\n"
            + "from user_interests_raw r2, users u\r\n"
            + "where r2.user_id = u.user_Id\r\n"
            + "and u.user_type = :userType \r\n"
            + "and r2.user_id <> :userId\r\n" +
            " and (u.about is not null or 'false' = :filterIncompleteProfiles)\n" +
            " and (u.brief_description is not null or 'false' = :filterIncompleteProfiles) \r\n"
            + "and r2.user_id not in (select connection_user_id from user_active_connections where user_id = :userId) "
            + "and r2.user_id not in (select user_id from user_active_connections where connection_user_id = :userId) "
            + "and r2.user_id not in (select connection_user_id from user_other_connections where user_id = :userId) "
            + "and r2.user_id not in (select user_id from user_other_connections where connection_user_id = :userId) "
            + "group by u.user_Id", nativeQuery = true)
    public List<Object[]> findRandomRecommendationsByUserId(String userId, String userType, boolean filterIncompleteProfiles);

    public Long deleteByUserId(String userId);

}

