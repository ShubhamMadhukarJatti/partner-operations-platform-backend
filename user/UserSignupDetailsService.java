package com.sharkdom.service.user;

import com.sharkdom.entity.user.UserSignupDetails;
import com.sharkdom.repository.user.UserSignupDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserSignupDetailsService {

    @Autowired
    private UserSignupDetailsRepository userSignupDetailsRepository;


    @Transactional
    public UserSignupDetails create(UserSignupDetails userAdditionalDetails) {
        return userSignupDetailsRepository.save(userAdditionalDetails);
    }

    public List<UserSignupDetails> findByUserEmail(String email) {
        return userSignupDetailsRepository.findAllByEmail(email);
    }

}
