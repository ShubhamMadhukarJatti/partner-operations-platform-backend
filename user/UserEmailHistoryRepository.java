package com.sharkdom.repository.user;

import com.sharkdom.entity.user.UserEmailHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEmailHistoryRepository extends JpaRepository<UserEmailHistory, Long> {
    @Query(value = "SELECT senderOrganizationId from UserEmailHistory where userId = :userId")
    List<Long> findAllByUserId(String userId);
}
