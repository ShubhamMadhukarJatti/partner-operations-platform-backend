package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "org_email_history")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EmailHistory extends BaseEntity {
    Long organizationId;
    Long senderOrganizationId;
}
