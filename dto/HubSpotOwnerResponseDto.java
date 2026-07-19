package com.sharkdom.partnerattribution.dto;

import lombok.*;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HubSpotOwnerResponseDto implements Serializable {

    private String email;

    private String firstName;

    private String lastName;
}