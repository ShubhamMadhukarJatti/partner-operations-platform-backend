package com.sharkdom.entity.campaign;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
@Builder
@Table(name = "trigger_flows")
@AllArgsConstructor
@RequiredArgsConstructor
public class TriggerFlow extends BaseEntity {
    @Column(columnDefinition = "LONGTEXT")
    private List<String> nodes;
    @Column(columnDefinition = "LONGTEXT")
    private List<String> edges;
    @OneToMany(mappedBy = "triggerFlow", cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Condition> conditions;
}
