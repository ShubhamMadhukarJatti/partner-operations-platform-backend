package com.sharkdom.entity.demo;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.model.demo.DemoType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serial;

@Entity
@Table(name = "demo_book")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemoBook extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;
    private String firstName;
    private String lastName;
    private String businessEmail;
    private String startupName;
    private String purpose;
    private String phoneNumber;
    private DemoType demoType;
}
