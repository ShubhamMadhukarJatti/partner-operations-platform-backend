package com.sharkdom.mypartner.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sharkdom.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "t_my_partner_send_credential")
public class MyPartnerSendCredential extends BaseEntity {

    @JsonProperty("url")
    @Column(name = "url")
    private String url;

    @JsonProperty("username")
    @Column(name = "username")
    private String username;

    @JsonProperty("password")
    @Column(name = "password")
    private String password;

    @JsonProperty("sender_id")
    @Column(name = "sender_id")
    private String senderId;

    @JsonProperty("receiver_id")
    @Column(name = "receiver_id")
    private String receiverId;
}
