package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "work_experience")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class WorkExperience extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String designation;
    private String companyName;
    private String startDate;
    private String endDate;
    private String description;
    private boolean currentCompany;

}