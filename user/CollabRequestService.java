package com.sharkdom.service.user;

import com.sharkdom.entity.user.Collab;
import com.sharkdom.repository.user.CollabRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CollabRequestService {

    @Autowired
    CollabRequestRepository collabRequesRepository;

    public List<Collab> findAllByUserId(String userId) {
        return collabRequesRepository.findAllByUserId(userId);
    }

    public List<Collab> findAllByReceiverUserId(String receiverUserId) {
        return collabRequesRepository.findAllByReceiverUserId(receiverUserId);
    }

    public Map<String, Integer> getCollabsCountForUsers(List<String> userIds) {
        return collabRequesRepository.getCountByReceiverUserIdsInOrUserIdsIn(userIds, userIds)
                .stream().collect(Collectors.toMap(e -> String.valueOf(e[0]), e -> Integer.parseInt(String.valueOf(e[1]))));
    }

    @Transactional
    public Collab createOrUpdate(Collab collabRequest) {
        return collabRequesRepository.save(collabRequest);
    }

    public Optional<Collab> findById(Long id) {
        return collabRequesRepository.findById(id);
    }
}
