package com.sharkdom.repository.organizationcollaboration;

import com.sharkdom.entity.organizationcollaboration.EnvelopeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface EnvelopeRepository extends JpaRepository<EnvelopeEntity, Long> {
    Optional<EnvelopeEntity> findByEnvelopeId(String envelopeId);
}
