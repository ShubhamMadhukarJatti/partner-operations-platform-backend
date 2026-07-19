package com.sharkdom.datapipeline.dto;

import com.sharkdom.model.ai.RecordType;
import lombok.Data;
import java.util.List;

@Data
public class HubspotMetadataRequest {
    private List<RecordType> recordTypes;
}
