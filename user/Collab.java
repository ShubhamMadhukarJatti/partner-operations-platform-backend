package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.constants.user.CollabStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "collab")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Collab extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String receiverUserId;
    private boolean watermarkAllowed;
    private boolean senderGuidlinesRequired;
    private boolean receiverGuidlinesRequired;
    @Column(length = 3000)
    private String receiverBenefitsJson;
    @Column(length = 3000)
    private String senderBenefitsJson;
    private CollabStatus status;
    @Column(length = 3000)
    private String userUrls;
    @Column(length = 3000)
    private String receiverUserUrls;

}
