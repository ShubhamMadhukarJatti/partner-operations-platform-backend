package com.sharkdom.repository.user;


import com.sharkdom.entity.user.UserAdvices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAdvicesRepository extends JpaRepository<UserAdvices, Long> {

    List<UserAdvices> findAllByUserId(String userId);

}