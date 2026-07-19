package com.sharkdom.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendedUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String name;
    private String briefDescription;
    private String userType;
    private String interests;
    private String username;

}
