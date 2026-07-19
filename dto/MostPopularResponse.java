package com.sharkdom.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MostPopularResponse {

    private Long organizationId;
    private Long mostPopular;
}
