package com.sharkdom.entity.suggestion;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "suggestions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionEntity extends BaseEntity {

    @Column(nullable = false)
    @JsonProperty("orgId")
    private String orgId;
    private String name;
    private String website;
    private String category;
    private String country;
}
