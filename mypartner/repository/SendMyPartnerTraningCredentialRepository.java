package com.sharkdom.mypartner.repository;

import com.sharkdom.mypartner.entity.MyPartnerSendTrainingCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SendMyPartnerTraningCredentialRepository extends JpaRepository<MyPartnerSendTrainingCredential, Long> {
    boolean existsBySenderIdAndReceiverId(Long receiverId, Long senderId);
}
