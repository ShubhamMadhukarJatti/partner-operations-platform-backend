package com.sharkdom.model.organization;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProgressResponse {
    private boolean status;
    private int value;

}
