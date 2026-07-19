package com.sharkdom.entity.user;

import com.sharkdom.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConnections extends BaseEntity {

    private static final long serialVersionUID = 1L;

    String userId;
    String connectionUserId;

}
