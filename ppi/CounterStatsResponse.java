package com.sharkdom.model.ppi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CounterStatsResponse {

    private Integer totalClicks;
    private Integer totalSubmits;
}
