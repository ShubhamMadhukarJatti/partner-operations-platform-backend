package com.sharkdom.partnerprogram.dtos;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerResourceDTO {

    private Long id;
    private String title;
    private String description;
    private String link;
    private Date created;
    private Date updated;
}
