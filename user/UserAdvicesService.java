package com.sharkdom.service.user;

import com.sharkdom.entity.user.UserAdvices;
import com.sharkdom.repository.user.UserAdvicesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAdvicesService {

    @Autowired
    UserAdvicesRepository userAdvicesRepository;

    public List<String> findAllByUserId(String userId) {
        return userAdvicesRepository.findAllByUserId(userId).stream().map(e -> e.getAdvice()).collect(Collectors.toList());
    }

    @Transactional
    public UserAdvices create(UserAdvices userAdvices) {
        return userAdvicesRepository.save(userAdvices);
    }
}
