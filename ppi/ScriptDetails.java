package com.sharkdom.entity.ppi;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name ="script_details")
@AllArgsConstructor
@NoArgsConstructor
public class ScriptDetails extends BaseEntity {

    private String scriptId;
    @Lob
    private String script;
    private String formId;
    private String sheetId;
    private String formName;
    private String sheetName;
    private String webhookUrl;
    private Long orgId;
    private String responderUrl;

}
