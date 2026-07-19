package com.sharkdom.entity.credits;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "t_credit")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credit extends BaseEntity {

    @JsonProperty("credits")
    @Column(name = "credits")
    private int credits;

    @JsonProperty("org_id")
    @Column(name = "org_id")
    private Long orgId;
}
