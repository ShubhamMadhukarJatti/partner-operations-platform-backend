package com.sharkdom.util.date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class DateUtil {

    @Value("${application.timezone}")
    private String timezone;

    public LocalDate getFutureDateAfterDays(Integer days) {
        return currentDate().plusDays(days);
    }

    public LocalDate currentDate() {
        return LocalDate.now(ZoneId.of(timezone));
    }
}
