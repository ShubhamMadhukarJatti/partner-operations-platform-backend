package com.sharkdom.model.ai;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.ai.CronStatus;
import com.sharkdom.model.ai.OverlapFrequency;
import com.sharkdom.model.ai.PersonaMode;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(
        name = "overlap_cron_execution",
        indexes = {
                @Index(name = "idx_org_persona_frequency", columnList = "organizationId, persona, frequency"),
                @Index(name = "idx_execution_time", columnList = "executionTime"),
                @Index(name = "idx_status", columnList = "status")
        }
)
@Data
public class OverlapCronExecutionEntity extends BaseEntity {

    /**
     * Organization for which cron executed
     */
    private Long organizationId;

    /**
     * Persona (CRM, HUBSPOT, SALESFORCE, etc.)
     */
    @Enumerated(EnumType.STRING)
    private PersonaMode persona;

    /**
     * Frequency (WEEKLY, MONTHLY, etc.)
     */
    @Enumerated(EnumType.STRING)
    private OverlapFrequency frequency;

    /**
     * Version created in overlap_records table
     */
    private Integer versionCreated;

    /**
     * SUCCESS, FAILED, RUNNING
     */
    @Enumerated(EnumType.STRING)
    private CronStatus status;

    /**
     * When cron started
     */
    private Instant executionTime;

    /**
     * When cron finished
     */
    private Instant completedTime;

    /**
     * Total records processed
     */
    private Long totalRecordsProcessed;

    /**
     * Total overlap found
     */
    private Long totalOverlapFound;

    /**
     * Error message if failed
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

}