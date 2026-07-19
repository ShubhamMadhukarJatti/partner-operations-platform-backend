package com.sharkdom.repository.user;

import com.sharkdom.entity.user.UserIpHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserIpHistoryRepository extends JpaRepository<UserIpHistory, Long> {
    Optional<UserIpHistory> findFirstByEmailOrderByLastLoginTimeDesc(String email);
}
