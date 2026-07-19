package com.sharkdom.model.ai;

public enum CronStatus {

    /**
     * Cron has started but not completed yet
     */
    RUNNING,

    /**
     * Cron completed successfully
     */
    SUCCESS,

    /**
     * Cron failed due to error
     */
    FAILED,

    /**
     * Cron was skipped (optional)
     */
    SKIPPED

}
