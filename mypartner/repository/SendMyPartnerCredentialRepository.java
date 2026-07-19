package com.sharkdom.mypartner.repository;

import com.sharkdom.mypartner.entity.MyPartnerSendCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SendMyPartnerCredentialRepository extends JpaRepository<MyPartnerSendCredential, Long> {
    List<MyPartnerSendCredential> findBySenderIdAndReceiverId(String senderId, String receiverId);
}
