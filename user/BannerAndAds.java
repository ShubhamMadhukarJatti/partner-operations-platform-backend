package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "banner_and_ads")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class BannerAndAds extends BaseEntity {

    private static final long serialVersionUID = 1L;

    String code;
    String type;
    @Column(length = 3000)
    String data;
    Date startFrom;
    Date endAt;
    boolean isActive;

}
