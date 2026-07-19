package com.sharkdom.repository.typeform;

import com.sharkdom.entity.typeform.TypeformEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeFormEventRepository extends JpaRepository<TypeformEvent, Long> {
}
