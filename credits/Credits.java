package com.sharkdom.entity.credits;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.organization.Organization;
import lombok.*;

import jakarta.persistence.*;

import java.io.Serial;

@Entity
@Table(name = "credits")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credits extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    private int playgroundLeft = 3;
    private int playgroundAllocated = 3;
    private int aiProposalLeft = 1;
    private int aiProposalAllocated = 1;
    private int collaborationsLeft = 4;
    private int collaborationsAllocated = 4;
    @OneToOne(fetch = FetchType.EAGER, mappedBy = "credits")
    @JsonIgnore
    @ToString.Exclude
    private Organization organization;
}
