package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.LogDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogDetailsRepository extends JpaRepository<LogDetails, Long> {


}
