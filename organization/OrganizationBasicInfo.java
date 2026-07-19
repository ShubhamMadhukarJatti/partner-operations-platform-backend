package com.sharkdom.entity.organization;

import com.sharkdom.entity.BaseEntity;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationBasicInfo extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private String name;
    private String briefDescription;
}
