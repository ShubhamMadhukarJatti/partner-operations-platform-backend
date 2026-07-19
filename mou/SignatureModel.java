package com.sharkdom.model.mou;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class SignatureModel {
    private List<String> onPages;
    private String position;
}

