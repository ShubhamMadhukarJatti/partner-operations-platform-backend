package com.sharkdom.entity.sales;

import com.sharkdom.entity.BaseEntity;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales_dashboard")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sales extends BaseEntity {


    private String leadName;
    private String leadDesignation;
    private String leadEmail;
    private String leadSource;

}
