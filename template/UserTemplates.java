package com.sharkdom.entity.template;

import com.sharkdom.entity.BaseEntity;
import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serial;

@Entity
@Table(name = "user_templates",
        uniqueConstraints = {@UniqueConstraint(name = "UniqueUserAndTemplate", columnNames = {"templateId", "userId"})})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserTemplates extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    Long templateId;
    String userId;

}
