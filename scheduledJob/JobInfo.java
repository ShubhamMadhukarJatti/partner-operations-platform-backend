package com.sharkdom.entity.scheduledJob;


import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class JobInfo {
    private String className;
    private String methodName;
    private String cron;
    private Long fixedDelay;
    private Long fixedRate;
}
