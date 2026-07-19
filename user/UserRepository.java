package com.sharkdom.repository.user;

import com.sharkdom.entity.user.User;
import com.sharkdom.model.user.UserEmail;
import com.sharkdom.model.user.UserEmailId;
import com.sharkdom.model.user.UserSearchResponseBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    public Optional<User> findByUserId(String userId);

    @Query(value = "SELECT * FROM users WHERE email = :email LIMIT 1", nativeQuery = true)
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    public Optional<User> findByUsername(String username);

    public List<User> findAllByStatus(String status);

    public boolean existsUserByUsername(String username);

    boolean existsByUserIdAndIsActiveTrue(String userId);

    boolean existsByEmail(String email);

    public void deleteByUsername(String username);

    public void deleteByUserId(String username);

    @Query(value = "select userId AS userId, username AS username, "
            + "briefDescription AS briefDescription, "
            + "userType AS userType, "
            + "tags AS tags, "
            + "city AS city, "
            + "state AS state "
            + "from User u "
            + "where u.username like %:partialUsername% "
            + "and u.tags like %:tags% "
            + "and (:city = '' OR upper(u.city) = upper(:city)) "
            + "and (:state = '' OR upper(u.state) = upper(:state))")
    public Page<UserSearchResponseBase> searchUser(String partialUsername, String tags, String city, String state, Pageable pageable);

    @Query(value = "select name AS name, "
            + "username AS username, "
            + "userType AS userType, "
            + "email AS email "
            + "from User u "
            + "where (:userType = '' OR upper(u.userType) = upper(:userType)) ")
    public Page<UserEmail> getEmailListByUserType(String userType, Pageable pageable);

    public List<User> findAllByUserIdIn(List<String> userIds);

    @Query(value = "SELECT data_value " +
            "FROM user_additional_details " +
            "where data_key = 'CPTYPE'", nativeQuery = true)
    public List<String> getAllCPTypes();

    @Query("SELECT u.userId FROM User u " +
            "LEFT JOIN OrganizationUserMapping oum " +
            "ON u.userId = oum.userId WHERE oum.userId IS NULL")
    List<String> findUsersWithoutOrganizationMapping();


    @Query("SELECT new com.sharkdom.model.user.UserEmailId(u.email, u.userId, u.name) FROM User u where u.status = 'ACTIVE'")
    List<UserEmailId> getAllUsersId();

    @Query("SELECT new com.sharkdom.model.user.UserEmailId(u.email, u.userId, u.name) FROM User u where u.status = 'ACTIVE' AND u.lastUpdatedTimestamp >= :updateAfter AND u.lastUpdatedTimestamp < :updateBefore")
    List<UserEmailId> getAllUsersIdBetweenDates(@Param("updateAfter") Date updateAfter, @Param("updateBefore") Date updateBefore);

    @Query("SELECT new com.sharkdom.model.user.UserEmailId(u.email, u.userId, u.name) " +
            "FROM User u " +
            "JOIN OrganizationUserMapping o ON u.userId = o.userId " +
            "WHERE o.organizationId = :orgId AND u.status = 'ACTIVE'")
    List<UserEmailId> getAllUsersByOrganizationId(@Param("orgId") Long orgId);

    List<User> findByUserIdIn(List<String> userIds);

}