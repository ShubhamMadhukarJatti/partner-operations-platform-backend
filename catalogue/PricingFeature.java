package com.sharkdom.entity.catalogue;

import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.*;

@Entity
@Table(name = "t_pricing_features")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingFeature extends BaseEntity {

    @Column(name = "feature_name", nullable = false)
    private String featureName;

}
