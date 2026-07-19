package com.sharkdom.repository.user;

import com.sharkdom.entity.user.UserLastActivityTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLastActivityTimeRepository extends JpaRepository<UserLastActivityTime, Long> {

}
