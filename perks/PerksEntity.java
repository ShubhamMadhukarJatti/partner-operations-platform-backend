package com.sharkdom.entity.perks;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;
import java.util.List;

@Entity
@Table(name = "perks")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerksEntity extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    private String perkDetails;
    private List<String> steps;
    private String perkValue;
    private String perkDuration;
    private int redeemedCount;
    private int clickedCount;
    private PerkStatus perkStatus;
    private String perkUrl;
    private String perkIcon;
    private String perkName;


}
