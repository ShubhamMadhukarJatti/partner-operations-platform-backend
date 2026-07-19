package com.sharkdom.model.ppi;

import lombok.Data;

@Data
public class GoogleDetailsResponse {

    private String scriptId;
    private String title;
    private String createTime;
    private String updateTime;
    private Creator creator;
    private LastModifyUser lastModifyUser;
}
