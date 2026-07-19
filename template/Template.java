package com.sharkdom.entity.template;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "template")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor

public class Template extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column(unique = true)
    private long templateId;
    private String title;
    private int used;
    private int saved;

    @Min(0)
    @Max(100)
    private int succcessRate;


    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "template_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<TemplateExpectation> templateExpectations;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "template_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<TemplateOfferings> templateOfferings;


    public void updateFields(Template updatedTemplate) {
        // Assuming you have other fields to update as well
        this.templateExpectations = updatedTemplate.getTemplateExpectations();
        this.templateOfferings = updatedTemplate.getTemplateOfferings();
        this.used = updatedTemplate.getUsed();
        this.saved = updatedTemplate.getSaved();
        this.succcessRate = updatedTemplate.getSucccessRate();
        // Update other fields as needed...
    }

}
