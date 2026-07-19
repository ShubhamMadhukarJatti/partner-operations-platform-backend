package com.sharkdom.entity.payment;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "stripe_callback")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StripEntity extends BaseEntity {
    @Column(columnDefinition = "LONGTEXT")
    private String details;
}
