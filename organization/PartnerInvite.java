package com.sharkdom.entity.organization;

import com.sharkdom.constants.user.GenericRecordStatus;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "partner_invite",uniqueConstraints = {@UniqueConstraint(columnNames = {"email"})})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PartnerInvite extends BaseEntity {
    private Long organizationId;
    @Column(unique = true)
    private String email;
    private GenericRecordStatus status;
    private String name;
}
