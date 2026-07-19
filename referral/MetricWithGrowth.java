package com.sharkdom.model.referral;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricWithGrowth {
    private int value;
    private int growth;
}