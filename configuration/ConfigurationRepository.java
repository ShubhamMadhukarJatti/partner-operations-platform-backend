package com.sharkdom.repository.configuration;

import com.sharkdom.entity.configuration.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigurationRepository extends JpaRepository<Configuration, Long> {

    List<Configuration> findAllActiveByTypeAndWebApplicableAndAppApplicableAndKeyIn(String type, boolean webApplicable, boolean appApplicable, List<String> keys);

    List<Configuration> findAllByTypeAndActiveIsTrue(String type);

    List<Configuration> findByActiveTrue();

    List<Configuration> findAllByType(String type);

    Optional<Configuration> findByKey(String key);

    List<Configuration> findAllByKeyAndType(String key, String type);

    Page<Configuration> findAllByTypeAndValueLikeIgnoreCase(String type, String value, Pageable pageable);

    List<Configuration> findAllByTypeIn(String[] type);

    List<Configuration> findAllByTypeInAndActiveIsTrue(String[] type);
}
