package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

//TODO drop this entity, it's not used anywhere
@Entity
@Table(name = "collab_request")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class CollabRequest extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String receiverUserId;
    private boolean watermarkAllowed;
    private boolean senderGuidlinesRequired;
    private boolean receiverGuidlinesRequired;
    private String receiverBenefitsJson;
    private String senderBenefitsJson;

}
