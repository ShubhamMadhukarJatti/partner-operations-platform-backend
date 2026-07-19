package com.sharkdom.model.email;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateEmailReqByOrganization {

    String templateCode;
    List<Long> orgIds;
    String s3AttachmentNames;

}
