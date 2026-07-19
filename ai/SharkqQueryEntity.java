package com.sharkdom.entity.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "sharkq_query_message")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharkqQueryEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    @Lob
    @Column (columnDefinition = "LONGTEXT")
    private String message;
    @Lob
    @Column (columnDefinition = "LONGTEXT")
    private String prompt;
    private Long organizationId;
    private boolean isBot;
    @Lob
    @Column (columnDefinition = "LONGTEXT")
    @JsonIgnore
    private String orgNames;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Map<String, Object>> orgNamesList;

}
