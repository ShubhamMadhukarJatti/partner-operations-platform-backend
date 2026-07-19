package com.sharkdom.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Data
@NoArgsConstructor
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Schema(name = "id", example = "1", description = "Auto generated Primary key, never send in POST request if you are trying to send new object, must sent in POST/PUT request if you are updating an existing object.")
    protected Long id;

    @CreationTimestamp
    @Column(name = "creationTimestamp")
    @Schema(name = "creationTimestamp", description = "Don't send in POST request")
    private Date creationTimestamp;

    @UpdateTimestamp
    @Column(name = "lastUpdatedTimestamp")
    @Schema(name = "lastUpdatedTimestamp", description = "Don't send in POST/PUT request")
    private Date lastUpdatedTimestamp;

}