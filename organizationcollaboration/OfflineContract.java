package com.sharkdom.entity.organizationcollaboration;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "offline_contract")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfflineContract extends BaseEntity {

    private static final long serialVersionUID = 1L;
    private String org1Email;
    private String org2Email;
    private String docLink;
}
