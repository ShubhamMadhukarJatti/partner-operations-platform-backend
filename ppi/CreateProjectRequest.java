package com.sharkdom.model.ppi;

import jakarta.persistence.Lob;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
public class CreateProjectRequest {

    private String title;
    @Lob
    @Transient
    private String accessToken;

}
