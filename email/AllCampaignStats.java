package com.sharkdom.model.email;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AllCampaignStats {
    private String templateCode;
    private LocalDate sentAt;
    private Long bounceCount;
    private Long openCount;
    private Long clickCount;
}
