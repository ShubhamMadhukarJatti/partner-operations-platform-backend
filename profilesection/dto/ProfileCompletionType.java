package com.sharkdom.profilesection.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProfileCompletionType {

    PARTNER_PROGRAM_PUBLISHED(40),
    DATA_SOURCE_CONNECTED(40),
    PROFILE_COMPLETED(20);

    private final int weight;
}