package com.sharkdom.repository.ppi;

import com.sharkdom.entity.ppi.CreateProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.beans.JavaBean;
import java.util.List;

@Repository
public interface CreateProjectPpiRepository extends JpaRepository<CreateProject,Long> {

//    CreateProject findByScriptId(String scriptId);

    List<CreateProject> findByOrgId(Long orgId);

    boolean existsByTitleAndOrgId(String title, Long orgId);
}
