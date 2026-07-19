package com.sharkdom.emailOutreach.repository;

import com.sharkdom.emailOutreach.entity.MailgunEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailgunEmailRepository extends JpaRepository<MailgunEmail, Long> {

    Optional<MailgunEmail> findByThreadToken(String threadToken);

    Optional<MailgunEmail> findTopByThreadTokenOrderByCreationTimestampDesc(String threadToken);

}
