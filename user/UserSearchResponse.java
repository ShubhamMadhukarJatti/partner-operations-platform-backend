package com.sharkdom.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResponse {

    private String username;
    private String userId;
    private String briefDescription;
    private String userType;
    private String tags;
    private String state;
    private String city;
    private int collabsCount;

    public UserSearchResponse(UserSearchResponseBase base, int collabsCount) {
        this.tags = base.getTags();
        this.city = base.getCity();
        this.briefDescription = base.getBriefDescription();
        this.state = base.getState();
        this.username = base.getUsername();
        this.userType = base.getUserType();
        this.collabsCount = collabsCount;
        this.userId = base.getUserId();
    }

}