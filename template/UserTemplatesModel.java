package com.sharkdom.model.template;

import lombok.Data;

import java.util.List;

@Data
public class UserTemplatesModel {
    List<Long> templateIds;
    String userId;
}
