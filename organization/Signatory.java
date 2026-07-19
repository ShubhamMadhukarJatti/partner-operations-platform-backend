package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "organization_signatory")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Signatory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private String signatory;
    private String endDate;
    private String surrenderedDin;
    private String dinOrPanNumber;
    private String beginDate;
}
