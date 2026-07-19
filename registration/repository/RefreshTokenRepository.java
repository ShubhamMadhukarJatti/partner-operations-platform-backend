package com.sharkdom.registration.repository;


import com.sharkdom.registration.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    RefreshTokenEntity findByToken(String token);

    void deleteByToken(String token);

    List<RefreshTokenEntity> findAllByUserId(String userId);
}
