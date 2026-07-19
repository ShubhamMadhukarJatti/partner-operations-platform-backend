package com.sharkdom.model.referral;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class Leads {
    LocalDate date;
    List<NameEmail> details;

}
