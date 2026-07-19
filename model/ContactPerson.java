package com.sharkdom.agenticai.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactPerson {
    private String name;
    private String title;
    private String linkedin;
}