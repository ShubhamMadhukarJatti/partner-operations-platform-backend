package com.sharkdom.entity.ppi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sharkdom.constants.ppi.FormStatus;
import com.sharkdom.entity.BaseEntity;
import com.sharkdom.entity.organization.Organization;
import jakarta.persistence.*;
import lombok.*;

@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name="ppi_form_link")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PpiEntity extends BaseEntity {

    private String formId;
    private String sheetId;
    private String scriptId;
    private String webBookUrl;
    private String projectTitle;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isGoogleFormConnected = false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isGoogleSheetConnected = false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isAppScriptUpdate = false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDeployed=false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isTriggerUpdated = false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isWebhook = false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isPublish=false;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isgoogleSheetUpdated=false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean IsScriptCodeGenerated =false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isFormSelected=false;
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isCompleted=false;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Organization organization;
}