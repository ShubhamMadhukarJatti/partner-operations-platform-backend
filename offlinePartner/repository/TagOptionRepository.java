package com.sharkdom.offlinePartner.repository;

import com.sharkdom.offlinePartner.entity.TagOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagOptionRepository extends JpaRepository<TagOption, Long> {

    List<TagOption> findByColumnId(Long columnId);
}