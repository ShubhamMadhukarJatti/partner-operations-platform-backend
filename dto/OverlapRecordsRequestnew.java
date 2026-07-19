package com.sharkdom.dto;

import com.sharkdom.entity.ai.ExternalPartnerOverlapRecordFieldEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverlapRecordsRequestnew {

    private List<ExternalPartnerOverlapRecordFieldEntity> listA;
    private List<ExternalPartnerOverlapRecordFieldEntity> listB;

}
