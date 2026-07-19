package com.sharkdom.entity.template;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "template_offerings")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class TemplateOfferings extends BaseEntity {

    private static final long serialVersionUID = 1L;

    String benefit;
    String description;
}
