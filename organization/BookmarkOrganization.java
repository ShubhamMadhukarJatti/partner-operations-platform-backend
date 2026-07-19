package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "bookmark_organization")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class BookmarkOrganization extends BaseEntity {
    Long organizationId;
    List<Long> partnerOrganizations;
}
