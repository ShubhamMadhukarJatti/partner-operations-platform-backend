package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_signup_details")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class UserSignupDetails extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String email;
    private String socialMediaName;

}