package com.sharkdom.zoho.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "crm_contacts")
@Getter
@Setter
public class CrmContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;

    private String zohoRecordId;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String leadSource;

    private String ownerName;
}
