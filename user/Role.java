package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import com.sharkdom.constants.user.UserRole;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class Role extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private UserRole role;

}
