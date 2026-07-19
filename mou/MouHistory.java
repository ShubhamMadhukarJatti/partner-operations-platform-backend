package com.sharkdom.entity.mou;

import com.sharkdom.constants.Flag;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mou_history")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouHistory extends BaseEntity {
    private Long organizationCollaborationId;
    private Long organizationId;
    private String envelopeId;
    private String pdfUrl;
    private Flag type;
    private boolean signed;
    @Transient
    private String organizationName;
}
