package com.sharkdom.model.email;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class EmailUpdateRequest {
    private String originalEmail;
    private String newEmail;
}
