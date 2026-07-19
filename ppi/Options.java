package com.sharkdom.entity.ppi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name="options")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Options extends BaseEntity {

    private Integer optionId;
    private String value;
    private Long formId;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internalQuestionPpi_id")
    private InternalQuestion_Ppi internalQuestionPpi;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internalResponsePpi_id")
    private InternalResponse_Ppi internalResponsePpi;

}
