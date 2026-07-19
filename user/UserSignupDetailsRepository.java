package com.sharkdom.repository.user;

import com.sharkdom.entity.user.UserSignupDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSignupDetailsRepository extends JpaRepository<UserSignupDetails, Long> {

    public List<UserSignupDetails> findAllByEmail(String email);

}